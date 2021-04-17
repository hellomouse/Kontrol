package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.BlockView;

import java.util.List;

public class C8051CoreBlock extends AbstractMUCCoreBlock {
    public C8051CoreBlock(Settings settings) {
        super(settings, "M8051F020");
    }

    @Override
    public void appendTooltip(ItemStack itemStack, BlockView world, List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(new TranslatableText("block.kontrol.c8051_core.tooltip"));
    }
}
