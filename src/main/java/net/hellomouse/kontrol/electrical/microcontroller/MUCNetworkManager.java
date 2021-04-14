package net.hellomouse.kontrol.electrical.microcontroller;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class MUCNetworkManager {
    // TODO: delete network, create network

    private static int networkId = 0;
    private static HashMap<BlockPos, AbstractMUCNetwork> posToNetworkMap = new HashMap<>();

    private static final ArrayList<AbstractMUCNetwork> networks = new ArrayList<>();

    public static void create(BlockPos pos, World world, Function<Integer, AbstractMUCNetwork> constructor) {
        if (world.isClient) return;

        // Generate new UUID for controller
        // Add netwok and floodfill

        AbstractMUCNetwork network = constructor.apply(networkId);
        network.createNetwork(pos, world);
        networks.add(network);
        networkId++;

        posToNetworkMap.put(pos, network);
    }

    public static void delete(BlockPos pos) {
        networks.remove(posToNetworkMap.get(pos));
        posToNetworkMap.remove(pos);
    }

    public static void tick() {
        for (AbstractMUCNetwork network : networks)
            network.tick();
    }
}
