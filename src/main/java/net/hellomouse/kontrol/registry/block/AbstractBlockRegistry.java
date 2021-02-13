package net.hellomouse.kontrol.registry.block;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.registry.util.BlockWrapper;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;


/**
 * A block-centered registry, but it also registers block items,
 * block entities, and block interface related elements.
 *
 * @author Bowserinator
 * @version 1.0
 */
public abstract class AbstractBlockRegistry {
    // All blocks
    private static final ArrayList<BlockWrapper> blocks = new ArrayList<>();
    // Lookup blocks by name
    private static final HashMap<String, BlockWrapper> blockLookup = new HashMap<>();
    // All colored Blocks & BlockItems
    private static final ArrayList<BlockWrapper> coloredBlocks = new ArrayList<>();
    // Unique block entity identifier (not necessarily same as the one passed into BlockEntity) : [Blocks that create this entity]
    private static final HashMap<String, ArrayList<Block>> blockEntityMap = new HashMap<>();

    /**
     * Adds block data to the registry. Use this method instead of directly
     * modifying the storage variables.
     * @param wrappedBlock A BlockWrapper instance
     */
    public static void addBlock(BlockWrapper wrappedBlock) {
        wrappedBlock.build(); // Gen-defaults, prevent future mutations
        blocks.add(wrappedBlock);
        blockLookup.put(wrappedBlock.getName(), wrappedBlock);

        if (wrappedBlock.hasColor())
            coloredBlocks.add(wrappedBlock);
        if (wrappedBlock.getBlockEntityName() != null) {
            String entityName = wrappedBlock.getBlockEntityName();
            blockEntityMap.computeIfAbsent(entityName, k -> new ArrayList<>());
            blockEntityMap.get(entityName).add(wrappedBlock.getBlock());
        }
    }

    /**
     * Call this method for registering client specific data
     * @see net.fabricmc.api.ClientModInitializer#onInitializeClient()
     */
    public static void registerClient() {
        for (BlockWrapper wrapper : coloredBlocks) {
            ColorData.COLOR_STRING color = wrapper.getColor();
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ColorData.DYEABLE_COLORS.get(color), wrapper.getItem());
            ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> ColorData.DYEABLE_COLORS.get(color), wrapper.getBlock());
        }
    }

    /**
     * Register a BlockEntity, returns the BlocksEntityType for assignment.
     * You will need to cast, ie:
     *
     * <pre>
     * {@code
     * MY_BLOCK_ENTITY = (BlockEntityType<MyBlockEntity>)getRegisteredBlockEntity(
     *      "my_block", "my_block_entity", MyBlockEntity::new);
     * }
     * </pre>
     *
     * @param blockEntityName Non-namespaced identifier string for block entity (ie, "furnace_generator")
     * @param blockEntityKey Unique key used in blockEntityMap when adding
     * @param supplier YourBlockEntity::new
     * @param <T> BlockEntity
     * @see BlockWrapper#blockEntityName(String)
     * @return Registered BlockEntityType
     */
    public static <T extends BlockEntity> BlockEntityType<? extends T> getRegisteredBlockEntity(String blockEntityName, String blockEntityKey, Supplier<? extends T> supplier) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Kontrol.MOD_ID, blockEntityName),
            BlockEntityType.Builder.create(supplier, blockEntityMap.get(blockEntityKey).toArray(new Block[0])).build(null));
    }

    /**
     * Call this method for registering general mod data
     * @see ModInitializer#onInitialize()
     */
    public static void register() {
        for (BlockWrapper wrapper : blocks) {
            String name = wrapper.getName();
            Registry.register(Registry.BLOCK, new Identifier(Kontrol.MOD_ID, name), wrapper.getBlock());
            Registry.register(Registry.ITEM,  new Identifier(Kontrol.MOD_ID, name), wrapper.getItem());
        }
    }

    /**
     * Lookup a block by name
     * @param name Name of the block
     * @return BlockWrapper, or null if not found
     */
    protected static BlockWrapper lookup(String name) {
        return blockLookup.get(name);
    }
}
