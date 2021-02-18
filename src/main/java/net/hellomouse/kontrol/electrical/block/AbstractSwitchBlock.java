package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.SwitchBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;


/**
 * Abstract switch block
 * @author Bowserinator
 */
@SuppressWarnings({"deprecation"})
public class AbstractSwitchBlock extends AbstractPolarizedElectricalBlock {
    public static final BooleanProperty OPEN = BooleanProperty.of("open");

    public AbstractSwitchBlock(AbstractBlock.Settings settings) {
        super(settings, true);
        setDefaultState(getStateManager().getDefaultState().with(OPEN, true));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new SwitchBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld)
            return ActionResult.PASS;
        else {
            SwitchBlockEntity switchBlockEntity = (SwitchBlockEntity) world.getBlockEntity(pos);


            // TODO: play sound


            if (switchBlockEntity != null) {
                switchBlockEntity.toggle();
                world.setBlockState(pos, state.with(OPEN, switchBlockEntity.isOpen()), 3);
            }
            return ActionResult.success(world.isClient);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(OPEN);
    }
}
