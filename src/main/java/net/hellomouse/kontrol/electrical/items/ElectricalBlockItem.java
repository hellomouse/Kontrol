package net.hellomouse.kontrol.electrical.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.util.FormatUtil;
import net.hellomouse.kontrol.util.TooltipUtil;
import net.hellomouse.kontrol.util.Units;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.util.List;

public class ElectricalBlockItem extends BlockItem {
    public ElectricalBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return new TranslatableText(this.getTranslationKey(stack));

        StringBuilder prefix = new StringBuilder();
        if (tag.get("Resistance") != null)
            prefix.append(FormatUtil.SIFormat(tag.getDouble("Resistance"), 0, Units.OHM));

        else if (tag.get("Capacitance") != null)
            prefix.append(FormatUtil.SIFormat(tag.getDouble("Capacitance"), 0, Units.FARAD));

        else if (tag.get("Inductance") != null)
            prefix.append(FormatUtil.SIFormat(tag.getDouble("Inductance"), 0, Units.HENRY));

        if (prefix.length() > 0)
            prefix.append(" ");

        LiteralText text = new LiteralText(prefix.toString());
        text.append(new TranslatableText(this.getTranslationKey(stack)));
        return text;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        CompoundTag tag = stack.getTag();

        if (tag == null)
            return;

        if (TooltipUtil.shiftTooltip(tooltip)) {
            tooltip.add(new LiteralText("Reeee"));
        }
    }
}
