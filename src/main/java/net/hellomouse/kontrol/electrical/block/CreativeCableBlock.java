package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.SuperconductingCableBlockEntity;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CreativeCableBlock extends AbstractWireBlock {
    public CreativeCableBlock(AbstractBlock.Settings settings, ColorData.COLOR_STRING color) {
        super(settings, 1.5f, color);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new SuperconductingCableBlockEntity().color(color);
    }
}
