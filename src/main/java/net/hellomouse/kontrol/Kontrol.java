package net.hellomouse.kontrol;

import net.fabricmc.api.ModInitializer;
import net.hellomouse.kontrol.registry.ElectricalBlockRegistry;
import net.hellomouse.kontrol.logic.circuit.CircuitManager;


/*
class MyBlock extends Block {
    public static final BooleanProperty UP =BooleanProperty.of("up");

    public MyBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(UP, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getStackInHand(hand).isEmpty()) {
            if (player.getStackInHand(hand).getItem() instanceof DyeItem) {
                ItemStack stack = player.getStackInHand(hand).copy();
                DyeColor color = ((DyeItem) stack.getItem()).getColor();
                if (color != state.get(COLOR)) {
                    stack.decrement(1);
                    player.setStackInHand(hand, stack);
                    world.setBlockState(pos, state.with(COLOR, color));
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.FAIL;
                }
            }
            if (player.getStackInHand(hand).getItem() instanceof StandardWrenchItem) {
                ItemStack stack = player.getStackInHand(hand).copy();
                stack.damage(1, world.random, player instanceof ServerPlayerEntity ? ((ServerPlayerEntity) player) : null);
                player.setStackInHand(hand, stack);
                world.setBlockState(pos, state.with(PULL, !state.get(PULL)));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    // https://www.programcreek.com/java-api-examples/?code=StellarHorizons%2FGalacticraft-Rewoven%2FGalacticraft-Rewoven-master%2Fsrc%2Fmain%2Fjava%2Fcom%2Fhrznstudio%2Fgalacticraft%2Fblock%2Fspecial%2Ffluidpipe%2FFluidPipeBlock.java
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction_1, BlockState blockState_2, WorldAccess world, BlockPos thisWire, BlockPos otherConnectable) {
        return state.with(getPropForDirection(direction_1), (
                !(blockState_2).isAir()
                        && blockState_2.getBlock() instanceof MyBlock//todo fluid things (network etc.)
        ));
    }

}*/

public class Kontrol implements ModInitializer {
    public final static String MOD_ID = "kontrol";

//    public static final ItemGroup KONTROL_ITEM_GROUP = FabricItemGroupBuilder.build(
//            new Identifier(MODID, "Kontrol"),
//            () -> new ItemStack(Blocks.COBBLESTONE));

    public static final CircuitManager circuitManager = new CircuitManager();

    @Override
    public void onInitialize() {
        ElectricalBlockRegistry.register();
        System.out.println("LOADED");


    }
}