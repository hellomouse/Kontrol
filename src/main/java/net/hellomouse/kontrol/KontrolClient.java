package net.hellomouse.kontrol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;

@Environment(EnvType.CLIENT)
public class KontrolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ElectricalBlockRegistry.registerClient();
        MUCBlockRegistry.registerClient();
    }
}
