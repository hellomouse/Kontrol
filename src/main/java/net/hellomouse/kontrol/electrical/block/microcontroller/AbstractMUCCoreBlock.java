package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.hellomouse.kontrol.config.KontrolConfig;
import net.hellomouse.kontrol.electrical.microcontroller.C8051.C8051Network;
import net.hellomouse.kontrol.electrical.microcontroller.MUCNetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings({"deprecation"})
public abstract class AbstractMUCCoreBlock extends Block {
    protected final String MUCID;

    public static final VoxelShape MUC_CORE_SHAPE = createCuboidShape(0, 0, 0, 16, 12, 16);

    public AbstractMUCCoreBlock(Settings settings, String MUCID) {
        super(settings);
        this.MUCID = MUCID;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        return MUC_CORE_SHAPE;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState blockstateOther, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        state = super.getStateForNeighborUpdate(state, dir, blockstateOther, world, pos, otherPos);

        return state;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        MUCNetworkManager.delete(pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.isClient) return;
        if (!MUCNetworkManager.create(pos, world, C8051Network::new) && placer instanceof PlayerEntity)
            ((PlayerEntity)placer).sendMessage(
                    new TranslatableText("muc_core.max_networks")
                            .append(new LiteralText(Formatting.RED + "" + KontrolConfig.getConfig().getMaxMUCNetworks()))
                            .append(new LiteralText(Formatting.RESET + "")), true);
    }
}
