package net.hellomouse.kontrol.registry.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.items.product_scanner.ProductScannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class MUCItemRegistry {
    public static final Item PRODUCT_SCANNER_ITEM = new ProductScannerItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1).rarity(Rarity.RARE));

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier(Kontrol.MOD_ID, "product_scanner"), PRODUCT_SCANNER_ITEM);
    }
}
