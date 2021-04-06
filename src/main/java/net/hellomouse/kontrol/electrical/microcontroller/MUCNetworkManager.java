package net.hellomouse.kontrol.electrical.microcontroller;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Function;

public class MUCNetworkManager {
    // TODO: delete network, create network

    private static int networkId = 0;

    private static final ArrayList<AbstractMUCNetwork> networks = new ArrayList<>();

    public static void create(BlockPos pos, World world, Function<Integer, AbstractMUCNetwork> constructor) {
        // Generate new UUID for controller
        // Add netwok and floodfill

        AbstractMUCNetwork network = constructor.apply(networkId);
        network.createNetwork(pos, world);
        networks.add(network);
        networkId++;
    }
}
