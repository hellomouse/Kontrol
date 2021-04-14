package net.hellomouse.kontrol.electrical.microcontroller;

import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCPortBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractMUCNetwork {
    // easier: just floodfill on UC controller place
    // get port => location map
    // try to check location

    public final int id;

    private World world;
    private final HashMap<Integer, MUCPortBlockEntity> portEntityMap = new HashMap<>();

    public AbstractMUCNetwork(int id) {
        this.id = id;
    }

    public void setPortVoltage(int portId, double voltage) {
        if (world.isClient) return;

        if (!portExists(portId)) return;
        portEntityMap.get(portId).setPortVoltage(voltage);
    }

    public double readPortVoltage(int portId) {
        if (!portExists(portId)) return 0.0;
        return portEntityMap.get(portId).getPortVoltage();
    }

    public abstract void tick();

    public boolean portExists(int portId) {
        if (!portEntityMap.containsKey(portId))
            return false;

        if (portEntityMap.get(portId).isRemoved()) {
            portEntityMap.remove(portId);
            return false;
        }
        return true;
    }

    public void createNetwork(BlockPos pos, World world) {
        this.world = world;
        if (world.isClient) return;

        Queue<BlockPos> toVisit = new LinkedList<>();
        HashSet<BlockPos> visited = new HashSet<>();
        toVisit.add(pos);

        // TODO: port count limit in config

        while (toVisit.size() > 0) {
            BlockPos p = toVisit.remove();

            if (visited.contains(p))
                continue;

            visited.add(p);

            // Check if port, add
            BlockEntity blockEntity = world.getBlockEntity(p);
            if (blockEntity instanceof MUCPortBlockEntity) {
                MUCPortBlockEntity portBlockEntity = (MUCPortBlockEntity)blockEntity;
                portEntityMap.put(portBlockEntity.getPortId(), portBlockEntity);
                System.out.println(portBlockEntity.getPortId() + " at " + portBlockEntity.getPos());
            }

            // Floodfill
            for (Direction dir : Direction.values()) {
                if (world.getBlockEntity(p.offset(dir)) instanceof MUCPortBlockEntity && !visited.contains(p.offset(dir)))
                    toVisit.add(p.offset(dir));
            }
        }
    }
}
