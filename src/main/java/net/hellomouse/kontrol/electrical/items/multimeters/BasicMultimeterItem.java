package net.hellomouse.kontrol.electrical.items.multimeters;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class BasicMultimeterItem extends AbstractMultimeterItem {
    public static final MultimeterToolMaterial MATERIAL = new MultimeterToolMaterial(8);
    public static final int FLAGS =
            MultimeterReading.FLAG_VOLTAGE |
            MultimeterReading.FLAG_DELTA_V;

    public BasicMultimeterItem(Item.Settings settings) {
        super(settings, "", FLAGS, 1, MATERIAL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        handleTooltips(tooltip, "basic_multimeter");
    }
}
