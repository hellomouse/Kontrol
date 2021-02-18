package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ButtonBlockEntity;
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


@SuppressWarnings({"deprecation"})
public abstract class AbstractPushButtonBlock extends AbstractPolarizedElectricalBlock {
    public static final BooleanProperty PRESSED = BooleanProperty.of("pressed");

    public AbstractPushButtonBlock(AbstractBlock.Settings settings) {
        super(settings, true);
        setDefaultState(getStateManager().getDefaultState().with(PRESSED, false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ButtonBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld)
            return ActionResult.PASS;
        else {
            ButtonBlockEntity entity = (ButtonBlockEntity) world.getBlockEntity(pos);
            entity.press();
            entity.onUpdate();
            System.out.println("PRESS " + entity.getResistance());

            return ActionResult.success(world.isClient);
        }
        //  return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(PRESSED);
    }



    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if ((Boolean)state.get(PRESSED)) {
            this.tryPowerWithProjectiles(state, world, pos);
        }
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && !(Boolean)state.get(PRESSED)) {
            this.tryPowerWithProjectiles(state, world, pos);
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        ButtonBlockEntity entity2 = (ButtonBlockEntity) world.getBlockEntity(pos);
        entity2.press();

        super.onSteppedOn(world, pos, entity);
    }

    private void tryPowerWithProjectiles(BlockState state, World world, BlockPos pos) {
        List<? extends Entity> list = world.getNonSpectatingEntities(PersistentProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos));
        boolean bl = !list.isEmpty();
        boolean bl2 = (Boolean) state.get(PRESSED);
        if (bl != bl2 && !world.isClient) {
            // TODO: also update the entity data

            world.setBlockState(pos, (BlockState) state.with(PRESSED, bl), 3);
            // this.playClickSound((PlayerEntity)null, world, pos, bl);
        }

        if (bl2) {
            ButtonBlockEntity entity = (ButtonBlockEntity) world.getBlockEntity(pos);
            entity.press();
        }

        if (bl) {
            world.getBlockTickScheduler().schedule(new BlockPos(pos), this, 10); // TODO
        }
    }
}
