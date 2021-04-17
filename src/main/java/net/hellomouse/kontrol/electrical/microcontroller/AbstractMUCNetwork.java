package net.hellomouse.kontrol.electrical.microcontroller;

import net.hellomouse.kontrol.config.KontrolConfig;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCPortBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCPortConnectorBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCPortBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A network of ports and a microcontroller core.
 * @author Bowserinator
 */
public abstract class AbstractMUCNetwork {
    // Unique numeric id
    public final int id;
    // World access
    private World world;
    // Map port id -> block entity
    private final HashMap<Integer, MUCPortBlockEntity> portEntityMap = new HashMap<>();

    /**
     * Construct an MUCNetwork. Should be called from MUCNetworkManager only
     * @param id ID assigned to this network
     */
    public AbstractMUCNetwork(int id) {
        this.id = id;
    }

    /**
     * Tick, the network should read / write to ports and
     * advance the interpreter state here.
     */
    public abstract void tick();

    /**
     * Max ports to search before floodfill terminates
     * @return Max ports to search before floodfill terminates
     */
    public abstract int maxPorts();

    /**
     * Can the core block floodfill in this direction? This only
     * applies to the core and not any ports
     * @param direction Direction
     * @return Can core floodfill in this direction?
     */
    public boolean coreCanFloodfillDir(Direction direction) { return true; }

    /**
     * Set the voltage of a given port, only sets it
     * if the port exists, otherwise does nothing. Only works
     * in the server world.
     *
     * @param portId id of the port
     * @param voltage Voltage value
     */
    public void setPortVoltage(int portId, double voltage) {
        if (world.isClient) return;
        if (portDoesNotExist(portId)) return;
        portEntityMap.get(portId).setPortVoltage(voltage);
    }

    /**
     * Read the voltage of a given port, only reads if
     * the port exists, otherwise returns 0.0 V. Only works
     * in the server world, returns 0.0 V otherwise.
     *
     * @param portId id of the port
     * @return Voltage of the port
     */
    public double readPortVoltage(int portId) {
        if (world.isClient) return 0.0;
        if (portDoesNotExist(portId)) return 0.0;
        return portEntityMap.get(portId).getPortVoltage();
    }

    /**
     * Returns if a port's block entity does not exist. If it does not exist
     * portEntityMap will be updated accordingly. Should only be
     * called in the server world.
     *
     * @param portId Id to check non-existence of
     * @return Does the port id not exist?
     */
    public boolean portDoesNotExist(int portId) {
        if (!portEntityMap.containsKey(portId))
            return true;
        if (portEntityMap.get(portId).isRemoved()) {
            portEntityMap.remove(portId);
            return true;
        }
        return false;
    }

    /** Called when network is deleted */
    public void onDelete() {
        for (MUCPortBlockEntity blockEntity : portEntityMap.values())
            world.setBlockState(blockEntity.getPos(), world.getBlockState(blockEntity.getPos()).with(MUCPortBlock.IN, false).with(MUCPortBlock.OUT, false).with(MUCPortBlock.BRIGHTNESS, 0));
    }

    /**
     * Construct the network via floodfill. Should only be called by MUCNetworkManager
     * and in a server world context. Client world calls will refuse to floodfill.
     *
     * @param pos Position of the MUC Core
     * @param world World access
     */
    public void createNetwork(BlockPos pos, World world) {
        this.world = world;
        if (world.isClient) return;

        Queue<BlockPos> toVisit = new LinkedList<>();
        HashSet<BlockPos> visited = new HashSet<>();
        toVisit.add(pos);

        int iterations = 0;
        int ports = 0;

        while (toVisit.size() > 0) {
            BlockPos p = toVisit.remove();
            visited.add(p);

            // Check if port, add
            BlockEntity blockEntity = world.getBlockEntity(p);
            if (blockEntity instanceof MUCPortBlockEntity) {
                MUCPortBlockEntity portBlockEntity = (MUCPortBlockEntity)blockEntity;
                portEntityMap.put(portBlockEntity.getPortId(), portBlockEntity);
                ports++;
            }

            if (ports > maxPorts()) return;
            if (iterations > KontrolConfig.getConfig().getMaxMUCFloodfill()) return;

            // Floodfill
            for (Direction dir : Direction.values()) {
                // Core direction limitations
                if (iterations == 0 && !coreCanFloodfillDir(dir))
                    continue;

                if ((world.getBlockEntity(p.offset(dir)) instanceof MUCPortBlockEntity ||
                     world.getBlockState(p.offset(dir)).getBlock() instanceof MUCPortConnectorBlock) &&
                        !visited.contains(p.offset(dir))) {
                    toVisit.add(p.offset(dir));
                    visited.add(p.offset(dir));
                }
            }

            iterations++;
        }
    }
}
