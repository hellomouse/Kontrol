package net.hellomouse.kontrol.registry.util;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import java.util.function.BiFunction;


/**
 * A wrapper class for Block data and metadata used in BlockRegistries.
 * You can create one by chaining the methods, for example:
 *
 * <pre>
 * {@code
 * new BlockWrapper()
 *    .name("uranium_ore")
 *    .block(new UraniumOreBlock(...))
 *    .blockEntityName("uranium_ore_entity")
 * }
 * </pre>
 *
 * <p>Block and name must be set or a NullPointErrorException will be thrown.
 * Any other components default to null and are optional.</p>
 *
 * <p>When you wish to finalize the wrapper, call .build(). This will create
 * default values for certain null components, and prevent modification of
 * the values (IllegalStateException will be thrown if you try to). By default,
 * AbstractBlockRegistry handles this automatically.
 * </p>
 *
 * @author Bowserinator
 * @version 1.0
 */
public class BlockWrapper {
    /* Saved properties - should have getter & setter */
    private Block block = null;
    private BlockItem item = null;
    private String name = null;
    private ColorData.COLOR_STRING color = null;
    private String blockEntityName = null;

    /* Additional properties - no getter / setter */
    private boolean finalizedProperties = false;

    /** Construct a BlockWrapper */
    public BlockWrapper() {}

    /**
     * Finalizes the wrapper, preventing future mutations
     * by setting finalizedProperties to true. Creates the
     * default BlockItem if not specified.
     *
     * @throws NullPointerException If block or name is not set when this is called
     */
    public void build() {
        if (block == null)
            throw new NullPointerException("Block cannot be null (Add a block with .block(...))");
        if (name == null)
            throw new NullPointerException("Name cannot be null (add a name with .name(...))");
        if (item == null)
            item = new BlockItem(block, new Item.Settings().group(ItemGroup.REDSTONE)); // Default item settings
        finalizedProperties = true;
    }

    /**
     * Does this block have a color property set?
     * @see #color(ColorData.COLOR_STRING)
     * @return Is a color set?
     */
    public boolean hasColor() { return this.color != null; }

    /**
     * Sets the block object, should be new WhateverYourBlock(...)
     * @param block Block object
     * @return this
     */
    public BlockWrapper block(Block block) {
        checkFinalized();
        this.block = block;
        return this;
    }

    /**
     * Sets the BlockItem object to use in place of the default
     * @see #build()
     * @param item BlockItem object
     * @return this
     */
    public BlockWrapper item(BlockItem item) {
        checkFinalized();
        this.item = item;
        return this;
    }

    /**
     * Alternative way to set BlockItem in place of the default, this will construct an instance
     * of the passed custom item using this.block and the provided item settings.
     *
     * <pre>{@code
     * new BlockWrapper()
     *   .block(...)
     *   .item(MyCustomItem::new, myFabricItemSettings)
     * }</pre>
     *
     * @see #build()
     * @param fn Constructor of the custom item, pass as MyCustomItem::new. Expects the constructor
     *           to take two arguments of type Block and Item.Settings and return a ? extends BlockItem type
     * @param settings Fabric item settings to apply to the item
     * @return this
     */
    public BlockWrapper item(BiFunction<Block, Item.Settings, BlockItem> fn, FabricItemSettings settings) {
        checkFinalized();
        if (this.block == null)
            throw new IllegalStateException("Block not set, call .block(...) before calling .item(supplier, settings)!");
        this.item = fn.apply(this.block, settings);
        return this;
    }

    /**
     * Sets block identifier. This DOES NOT include the MOD_ID, ie,
     * if your block is "mod:grass", use .name("grass"), not .name("mod:grass")
     * @param name Block identifier without namespace
     * @return this
     */
    public BlockWrapper name(String name) {
        checkFinalized();
        this.name = name;
        return this;
    }

    /**
     * Sets the color variant. The color variant must be one of the 16
     * dye colors in ColorData.COLOR_STRING enum. When this is set, the
     * color variant for the block will be automatically generated. This is
     * done using fabric's ColorProviderRegistry and is client side. The
     * color name is not automatically appended, so call .name() accordingly
     *
     * @see #name(String)
     * @param color Color enum variant to create
     * @return this
     */
    public BlockWrapper color(ColorData.COLOR_STRING color) {
        checkFinalized();
        this.color = color;
        return this;
    }

    /**
     * Key to use for creating block entities. This is only for reference,
     * implementation details are up to the registry itself
     * @param blockEntityName Unique name for block entity
     * @return this
     */
    public BlockWrapper blockEntityName(String blockEntityName) {
        checkFinalized();
        this.blockEntityName = blockEntityName;
        return this;
    }

    /** Returns block */
    public Block getBlock() { return block; }

    /** Returns item */
    public BlockItem getItem() { return item; }

    /** Returns name */
    public String getName() { return name; }

    /** Returns color */
    public ColorData.COLOR_STRING getColor() { return color; }

    /** Returns block entity name */
    public String getBlockEntityName() { return blockEntityName; }

    /**
     * Helper method to throw exception if trying to mutate when properties
     * are already finalized
     * @throws IllegalStateException If already finalized (using .build(), not java's destructor)
     */
    private void checkFinalized() {
        if (finalizedProperties)
            throw new IllegalStateException("BlockWrapper already finalized");
    }
}
