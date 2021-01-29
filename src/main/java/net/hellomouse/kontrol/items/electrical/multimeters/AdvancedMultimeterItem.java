package net.hellomouse.kontrol.items.electrical.multimeters;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class AdvancedMultimeterItem extends AbstractMultimeterItem {
    public static final MultimeterToolMaterial MATERIAL = new MultimeterToolMaterial(16);
    public static final int FLAGS =
            MultimeterReading.FLAG_VOLTAGE |
            MultimeterReading.FLAG_DELTA_V |
            MultimeterReading.FLAG_COLORED |
            MultimeterReading.FLAG_CURRENT |
            MultimeterReading.FLAG_POWER |
            MultimeterReading.FLAG_POLARITY;

    public AdvancedMultimeterItem(Item.Settings settings) {
        super(settings, "", FLAGS, 2, MATERIAL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        handleTooltips(tooltip, "advanced_multimeter");
    }
}
