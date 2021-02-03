package net.hellomouse.kontrol.electrical.items.multimeters;

import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.util.TooltipUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * A multimeter item, all multimeter items should extend this
 * @author Bowserinator
 */
public class AbstractMultimeterItem extends MiningToolItem {
    // Indent to lines after the first lines
    private String newLineIndent = "";
    // Multimeter data parse flags
    private int flags = 0x0;
    // Decimal point precision
    private int precision = 3;

    /**
     * Construct a multimeter item
     * @param settings Item settings
     */
    public AbstractMultimeterItem(Item.Settings settings, String newLineIndent, int flags, int precision, MultimeterToolMaterial material) {
        super(0.5f, -2.8f, material, new HashSet<>(), settings);
        this.newLineIndent = newLineIndent;
        this.flags = flags;
        this.precision = precision;
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
            ItemStack stack = context.getPlayer().getStackInHand(context.getHand());
            BlockEntity entity = world.getBlockEntity(context.getBlockPos());

            if (entity instanceof AbstractElectricalBlockEntity) {
                ArrayList<Text> result = ((AbstractElectricalBlockEntity) entity).getReading().parse(flags, precision);
                MultimeterReading.damageLines(result, (float)stack.getDamage() / stack.getMaxDamage());

                for (int i = 0; i < result.size(); i++) {
                    Text text = result.get(i);

                    // Add indent to non-first lines if enabled
                    if (i > 0 && newLineIndent.length() > 0)
                        text = new LiteralText(newLineIndent).append(text);

                    context.getPlayer().sendMessage(text, false);
                }
            }
        }
        return ActionResult.PASS;
    }

    /**
     * Helper for automatically adding consistent tooltips
     * for multimeter items
     * @param tooltip Tooltip list to add to
     * @param itemId Item ID, used in translation keys, ie item.mod_id.[item id]....
     */
    public void handleTooltips(List<Text> tooltip, String itemId) {
        String itemPrefix = "item." + Kontrol.MOD_ID + ".";

        if (TooltipUtil.shiftTooltip(tooltip)) {
            tooltip.add(new TranslatableText(itemPrefix + itemId + ".tooltip"));
            tooltip.add(new TranslatableText(itemPrefix + "multimeter.tooltip.usage"));
            tooltip.add(new TranslatableText(itemPrefix + "multimeter.precision").append(new LiteralText(String.valueOf(precision))));
        }
    }

    /**
     * Tool material class. This doesn't really do anything but offer
     * a way to assign a durability to a multimeter
     */
    protected static class MultimeterToolMaterial implements ToolMaterial {
        private final int durability;

        public MultimeterToolMaterial(int durability) {
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
