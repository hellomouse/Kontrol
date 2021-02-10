package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.InductorBlockEntity;
import net.hellomouse.kontrol.util.WorldUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation"})
public class CreativeInductorBlock extends AbstractPolarizedElectricalBlock {
    public CreativeInductorBlock(AbstractBlock.Settings settings) {
        super(settings, false);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new InductorBlockEntity();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        boolean NS = facing == Direction.NORTH || facing == Direction.SOUTH;

        VoxelShape base = NS ?
                createCuboidShape(3, 0, 0, 13, 1, 16) :
                createCuboidShape(0, 0, 3, 16, 1, 13);
        VoxelShape top = NS ?
                createCuboidShape(6, 0, 0, 10, 13, 16) :
                createCuboidShape(0, 1, 6, 16, 13, 10);
        return VoxelShapes.union(base, top);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InductorBlockEntity) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                itemStack.getOrCreateTag().putDouble("Inductance", ((InductorBlockEntity) blockEntity).getInductance());
                WorldUtil.spawnItemStack(world, pos, itemStack);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof InductorBlockEntity) {
            InductorBlockEntity inductorBlockEntity = (InductorBlockEntity) blockEntity;
            CompoundTag compoundTag = itemStack.getOrCreateTag();
            if (compoundTag.contains("Inductance")) {
                inductorBlockEntity.setInductance(compoundTag.getDouble("Inductance"));
                inductorBlockEntity.markDirty();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        InductorBlockEntity inductorBlockEntity = (InductorBlockEntity)world.getBlockEntity(pos);

        if (inductorBlockEntity != null)
            itemStack.getOrCreateTag().putDouble("Inductance", inductorBlockEntity.getInductance());
        return itemStack;
    }
}
