package net.hellomouse.kontrol.items.electrical.multimeters;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class OmnimeterItem extends AbstractMultimeterItem {
    public static final MultimeterToolMaterial MATERIAL = new MultimeterToolMaterial(-1);

    public OmnimeterItem(Item.Settings settings) {
        super(settings, "   ", MultimeterReading.FLAG_ALL, 4, MATERIAL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        handleTooltips(tooltip, "omnimeter");
    }
}
