package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.FurnaceGeneratorEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings({"deprecation"})
public class FurnaceGenerator extends AbstractPolarizedElectricalBlock implements BlockEntityProvider {
    public static final BooleanProperty LIT = BooleanProperty.of("lit");

    public FurnaceGenerator(AbstractBlock.Settings settings) {
        super(settings, false);
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(LIT, false)
                .with(Properties.WATERLOGGED, false));
    }


    // -- Terminal computation -- \\
    // Should match textures of the block

    // ---- Block behavior ---- \\
    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        // Coal generator is hot when on
        if (world.getBlockState(pos).get(LIT)) {
            if (!entity.isFireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity))
                entity.damage(DamageSource.HOT_FLOOR, 1.0F);
        }

        super.onSteppedOn(world, pos, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Smoke when water on top
        BlockPos blockPos = pos.up();
        if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.25D, (double)blockPos.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // RUN EVERY TICK TODO
        super.scheduledTick(state, world, pos, random);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new FurnaceGeneratorEntity();
    }


    // --- GUI --- \\
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
            //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null)
                player.openHandledScreen(screenHandlerFactory);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FurnaceGeneratorEntity) {
                ItemScatterer.spawn(world, pos, (FurnaceGeneratorEntity)blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity == null ? false : blockEntity.onSyncedBlockEvent(type, data);
    }

    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
    }


    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }


    // --- Rotation --- \\
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        super.appendProperties(stateManager);
        stateManager.add(LIT);
    }


    // --- World interaction --- \\


}
