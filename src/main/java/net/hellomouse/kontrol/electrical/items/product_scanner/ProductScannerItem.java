package net.hellomouse.kontrol.electrical.items.product_scanner;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.Kontrol;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Scan items and get "product" information
 * @author Bowserinator
 */
public class ProductScannerItem extends MiningToolItem {
    /**
     * Construct a multimeter item
     * @param settings Item settings
     */
    public ProductScannerItem(Item.Settings settings) {
        super(0.5f, -2.8f, new ProductToolMaterial(24), new HashSet<>(), settings);
    }

    /**
     * Use on block action (measure electrical item). Only works if
     * a player exists in the ItemUsageContext
     * @param context Context
     * @return ActionResult
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();

        if (!world.isClient() && context.getPlayer() != null) {
            BlockEntity entity = world.getBlockEntity(context.getBlockPos());
            if (entity instanceof IProductScanable) {
                ArrayList<Text> result = ((IProductScanable) entity).productInfo();
                for (Text text : result)
                    context.getPlayer().sendMessage(text, false);
            }
        }
        return ActionResult.PASS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("item." + Kontrol.MOD_ID + ".product_scanner.tooltip.usage"));
    }

    /**
     * Tool material class. This doesn't really do anything but offer
     * a way to assign a durability to a scanner
     */
    protected static class ProductToolMaterial implements ToolMaterial {
        private final int durability;

        public ProductToolMaterial(int durability) {
            this.durability = durability;
        }

        @Override
        public int getDurability() { return durability; }

        @Override
        public float getMiningSpeedMultiplier() { return 1.0f; }

        @Override
        public float getAttackDamage() { return 0.0f; }

        @Override
        public int getMiningLevel() { return 0; }

        @Override
        public int getEnchantability() { return 0; }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(Items.AIR);
        }
    }
}
