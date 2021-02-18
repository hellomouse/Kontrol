package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.CapacitorBlockEntity;
import net.hellomouse.kontrol.util.WorldUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"deprecation"})
public class CreativeCapacitorBlock extends AbstractPolarizedElectricalBlock {
    public CreativeCapacitorBlock(AbstractBlock.Settings settings) {
        super(settings, false);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new CapacitorBlockEntity();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING);
        boolean NS = facing == Direction.NORTH || facing == Direction.SOUTH;

        VoxelShape base = NS ?
                createCuboidShape(2, 0, 0, 14, 1, 16) :
                createCuboidShape(0, 0, 2, 16, 1, 14);
        VoxelShape top = NS ?
                createCuboidShape(4, 0, 1, 12, 12, 15) :
                createCuboidShape(1, 1, 4, 15, 12, 12);
        return VoxelShapes.union(base, top);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CapacitorBlockEntity) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                itemStack.getOrCreateTag().putDouble("Capacitance", ((CapacitorBlockEntity) blockEntity).getCapacitance());
                WorldUtil.spawnItemStack(world, pos, itemStack);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof CapacitorBlockEntity) {
            CapacitorBlockEntity capacitorBlockEntity = (CapacitorBlockEntity) blockEntity;
            CompoundTag compoundTag = itemStack.getOrCreateTag();
            if (compoundTag.contains("Capacitance")) {
                capacitorBlockEntity.setCapacitance(compoundTag.getDouble("Capacitance"));
                capacitorBlockEntity.markDirty();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        CapacitorBlockEntity capacitorBlockEntity = (CapacitorBlockEntity)world.getBlockEntity(pos);

        if (capacitorBlockEntity != null)
            itemStack.getOrCreateTag().putDouble("Capacitance", capacitorBlockEntity.getCapacitance());
        return itemStack;
    }
}
