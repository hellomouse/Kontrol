package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCPortMakerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("deprecation")
public class CreativeMUCPortMakerBlock extends BlockWithEntity {
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

    public CreativeMUCPortMakerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(TRIGGERED, false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new CreativeMUCPortMakerBlockEntity();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null)
                player.openHandledScreen(screenHandlerFactory);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        boolean bl2 = state.get(TRIGGERED);
        if (bl && !bl2) {
            world.getBlockTickScheduler().schedule(pos, this, 4);
            world.setBlockState(pos, state.with(TRIGGERED, true), 4);
        } else if (!bl && bl2) {
            world.setBlockState(pos, state.with(TRIGGERED, false), 4);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        boolean bl2 = state.get(TRIGGERED);
        if (bl && !bl2) {
            world.getBlockTickScheduler().schedule(pos, this, 4);
            world.setBlockState(pos, state.with(TRIGGERED, true), 4);
        } else if (!bl && bl2) {
            world.setBlockState(pos, state.with(TRIGGERED, false), 4);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CreativeMUCPortMakerBlockEntity)
            ((CreativeMUCPortMakerBlockEntity)blockEntity).create();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TRIGGERED);
    }
}
