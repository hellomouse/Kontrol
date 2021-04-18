package net.hellomouse.kontrol;

import net.fabricmc.api.ModInitializer;
import net.hellomouse.kontrol.config.KontrolConfig;
import net.hellomouse.kontrol.registry.block.AbstractBlockRegistry;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.hellomouse.kontrol.registry.item.ElectricalItemRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Kontrol implements ModInitializer {
    public static final String MOD_ID = "kontrol";
    public static final String MOD_NAME = "Kontrol";

    public static final KontrolConfig CONFIG = KontrolConfig.getConfig();
    public static final Logger LOG = LogManager.getLogger(Kontrol.MOD_ID);

    @Override
    public void onInitialize() {
        ElectricalBlockRegistry.register();
        ElectricalItemRegistry.register();

        MUCBlockRegistry.register();
        AbstractBlockRegistry.register();

        System.out.println("LOADED");


    }
}