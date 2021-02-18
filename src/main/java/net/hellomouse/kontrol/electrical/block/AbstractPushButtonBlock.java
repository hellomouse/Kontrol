package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.PushButtonBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;


/**
 * Abstract PushButtonBlock
 * @author Bowserinator
 */
@SuppressWarnings({"deprecation"})
public abstract class AbstractPushButtonBlock extends AbstractPolarizedElectricalBlock {
    public static final BooleanProperty PRESSED = BooleanProperty.of("pressed");

    // Ticks to stay down when pushed
    protected int pushTime = CircuitValues.DEFAULT_PUSH_BUTTON_PUSH_TIME;

    public AbstractPushButtonBlock(AbstractBlock.Settings settings) {
        super(settings, true);
        setDefaultState(getStateManager().getDefaultState().with(PRESSED, false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) { return new PushButtonBlockEntity().pushTime(pushTime); }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld)
            return ActionResult.PASS;
        else {
            press(world, pos);
            return ActionResult.success(world.isClient);
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        press(world, pos);
        super.onSteppedOn(world, pos, entity);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PRESSED))
            this.tryPowerWithProjectiles(state, world, pos);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && !(Boolean)state.get(PRESSED))
            this.tryPowerWithProjectiles(state, world, pos);
    }

    private void tryPowerWithProjectiles(BlockState state, World world, BlockPos pos) {
        List<? extends Entity> list = world.getNonSpectatingEntities(PersistentProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos));
        boolean entityFound = !list.isEmpty();
        boolean isPressed = state.get(PRESSED);

        if (entityFound != isPressed && !world.isClient) {
            world.setBlockState(pos, state.with(PRESSED, entityFound), 3);
            // this.playClickSound((PlayerEntity)null, world, pos, bl);
        }

        if (isPressed)
            press(world, pos);
        if (entityFound)
            world.getBlockTickScheduler().schedule(new BlockPos(pos), this, (int)Math.max(1, pushTime - 1));
    }

    private void press(World world, BlockPos pos) {
        PushButtonBlockEntity pushButtonBlockEntity = (PushButtonBlockEntity) world.getBlockEntity(pos);
        if (pushButtonBlockEntity != null)
            pushButtonBlockEntity.press();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(PRESSED);
    }
}
