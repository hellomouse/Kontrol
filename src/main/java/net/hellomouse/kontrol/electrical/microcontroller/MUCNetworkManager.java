package net.hellomouse.kontrol.electrical.microcontroller;

import net.hellomouse.kontrol.config.KontrolConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Microcontroller network manager
 * @author Bowserinator
 */
public class MUCNetworkManager {
    // Current free networkId, never decremented
    private static int freeNetworkId = 0;
    // Maps positions of a core to a network
    private static final HashMap<BlockPos, AbstractMUCNetwork> posToNetworkMap = new HashMap<>();
    // All networks
    private static final ArrayList<AbstractMUCNetwork> networks = new ArrayList<>();

    /**
     * Construct a network
     * @param pos Position of the core block
     * @param world World access, should be a server world. Client worlds ignored
     * @param constructor Constructor for the network type, should take 1 argument for network id
     * @return Creation success?
     */
    public static boolean create(BlockPos pos, World world, Function<Integer, AbstractMUCNetwork> constructor) {
        if (world.isClient) return false;

        if (networks.size() + 1 > KontrolConfig.getConfig().getMaxMUCNetworks()) {
            // TODO: warn client
            return false;
        }

        AbstractMUCNetwork network = constructor.apply(freeNetworkId);
        network.createNetwork(pos, world);
        networks.add(network);
        posToNetworkMap.put(pos, network);
        freeNetworkId++;
        return true;
    }

    /**
     * Delete a network from the network manager
     * @param pos Position of the core block
     */
    public static void delete(BlockPos pos) {
        AbstractMUCNetwork network = posToNetworkMap.getOrDefault(pos, null);
        if (network == null) return;

        network.onDelete();
        networks.remove(network);
        posToNetworkMap.remove(pos);
    }

    /** Tick all networks */
    public static void tick() {
        for (AbstractMUCNetwork network : networks)
            network.tick();
    }
}
