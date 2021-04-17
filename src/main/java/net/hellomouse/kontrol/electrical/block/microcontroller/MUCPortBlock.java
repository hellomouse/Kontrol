package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.hellomouse.kontrol.electrical.block.AbstractElectricalBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCPortBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.world.BlockView;

public class MUCPortBlock extends AbstractElectricalBlock {
    public static final BooleanProperty IN  = BooleanProperty.of("in");
    public static final BooleanProperty OUT = BooleanProperty.of("out");
    public static final IntProperty BRIGHTNESS = Properties.POWER;

    public MUCPortBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(IN, false).with(OUT, false).with(BRIGHTNESS, 0));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new MUCPortBlockEntity();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(IN).add(OUT).add(BRIGHTNESS);
    }
}
