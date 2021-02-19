package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.LEDBlockEntity;
import net.hellomouse.kontrol.electrical.misc.TemplateVoxelShapes;
import net.hellomouse.kontrol.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;


@SuppressWarnings({"deprecation"})
public class BasicLEDBlock extends AbstractLightBlock {
    public static final VoxelShape CUBOID_BASE;

    static {
        CUBOID_BASE = VoxelShapes.union(
                TemplateVoxelShapes.BASIC_PLATE_SHAPE,
                createCuboidShape(7.5, 1, 6, 8.5, 8, 7),
                createCuboidShape(7.5, 1, 9, 8.5, 8, 10),
                createCuboidShape(5, 9, 5, 11, 16, 11),
                createCuboidShape(4, 8, 4.5, 12, 9, 12));
    }

    private LEDBlockEntity.LEDData data;

    public BasicLEDBlock(AbstractBlock.Settings settings) {
        super(settings, true);
    }

    public BasicLEDBlock LEDData(LEDBlockEntity.LEDData data) {
        this.data = data;
        return this;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING);
        return VoxelShapeUtil.rotateShape(facing, CUBOID_BASE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new LEDBlockEntity().forwardVoltage(data != null ? data.forwardVoltage : 2.8);
    }

    @Override
    public int getBrightness(double voltage, double current, double power, double temperature) {
        // 600 is estimate of linear approximation of relative brightness (15 = 100%) vs current (A)
        return (int)(current * 600);
    }

    public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Environment(EnvType.CLIENT)
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}
