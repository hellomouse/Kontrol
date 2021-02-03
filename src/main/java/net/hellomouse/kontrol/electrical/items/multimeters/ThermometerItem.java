package net.hellomouse.kontrol.electrical.items.multimeters;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class ThermometerItem extends AbstractMultimeterItem {
    public static final MultimeterToolMaterial MATERIAL = new MultimeterToolMaterial(10);
    public static final int FLAGS =
            MultimeterReading.FLAG_COLORED |
            MultimeterReading.FLAG_TEMPERATURE;

    public ThermometerItem(Item.Settings settings) {
        super(settings, "", FLAGS, 2, MATERIAL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        handleTooltips(tooltip, "thermometer");
    }
}
