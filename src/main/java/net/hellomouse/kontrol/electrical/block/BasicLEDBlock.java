package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.LEDBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;


public class BasicLEDBlock extends AbstractLightBlock {
    public BasicLEDBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new LEDBlockEntity().forwardVoltage(2.8);
    }


    @Override
    public int getBrightness(double voltage, double current, double power, double temperature) {
        // TODO: note http://www1.futureelectronics.com/doc/EVERLIGHT%C2%A0/334-15__T1C1-4WYA.pdf
        return (int)(current * 600);
    }
}
