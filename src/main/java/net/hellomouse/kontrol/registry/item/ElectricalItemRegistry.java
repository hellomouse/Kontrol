package net.hellomouse.kontrol.registry.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.items.multimeters.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class ElectricalItemRegistry {
    // Multimeters
    public static final Item BASIC_MULTIMETER_ITEM = new BasicMultimeterItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1));
    public static final Item ADVANCED_MULTIMETER_ITEM = new AdvancedMultimeterItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1));
    public static final Item ELITE_MULTIMETER_ITEM = new EliteMultimeterItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1).rarity(Rarity.UNCOMMON));
    public static final Item ULTIMATE_MULTIMETER_ITEM = new UltimateMultimeterItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1).rarity(Rarity.RARE));
    public static final Item THERMOMETER_ITEM = new ThermometerItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1));
    public static final Item OMNIMETER_ITEM = new OmnimeterItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1).rarity(Rarity.EPIC));

    public static void register() {
        // Multimeters
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "thermometer"), THERMOMETER_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "basic_multimeter"), BASIC_MULTIMETER_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "advanced_multimeter"), ADVANCED_MULTIMETER_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "elite_multimeter"), ELITE_MULTIMETER_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "ultimate_multimeter"), ULTIMATE_MULTIMETER_ITEM);
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "omnimeter"), OMNIMETER_ITEM);
    }

    public static void registerClient() {

    }
}
