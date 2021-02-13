package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"deprecation"})
public class CreativeResistorBlock extends AbstractPolarizedElectricalBlock {
    public CreativeResistorBlock(AbstractBlock.Settings settings) {
        super(settings, false);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ResistorBlockEntity();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ?
                createCuboidShape(3, 0, 0, 13, 11, 16) :
                createCuboidShape(0, 0, 3, 16, 11, 13);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ResistorBlockEntity) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                itemStack.getOrCreateTag().putDouble("Resistance", ((ResistorBlockEntity) blockEntity).getResistance());
                WorldUtil.spawnItemStack(world, pos, itemStack);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ResistorBlockEntity) {
            ResistorBlockEntity resistorBlockEntity = (ResistorBlockEntity) blockEntity;
            CompoundTag compoundTag = itemStack.getOrCreateTag();
            if (compoundTag.contains("Resistance")) {
                resistorBlockEntity.setResistance(compoundTag.getDouble("Resistance"));
                resistorBlockEntity.markDirty();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        ResistorBlockEntity resistorBlockEntity = (ResistorBlockEntity)world.getBlockEntity(pos);

        if (resistorBlockEntity != null)
            itemStack.getOrCreateTag().putDouble("Resistance", resistorBlockEntity.getResistance());
        return itemStack;
    }
}
