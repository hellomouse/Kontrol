package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.util.ImplementedInventory;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualVoltageSource;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.hellomouse.kontrol.electrical.block.FurnaceGenerator;
import net.hellomouse.kontrol.electrical.screen.BoxScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;


public class FurnaceGeneratorEntity extends AbstractPolarizedElectricalBlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory, Tickable {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private int fuelTime;

    public FurnaceGeneratorEntity() {
        super(ElectricalBlockRegistry.FURNACE_GENERATOR_ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int[] getAvailableSlots(Direction dir) {
        // Just return an array of all slots
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        Inventories.toTag(tag, items);
        return super.toTag(tag);
    }

    @Override
    public void tick() {
        super.tick();

        boolean dirty = false;
        boolean burningInitialState = fuelTime > 0;
        boolean burningFinalState;

        if (!this.world.isClient) {
            ItemStack itemStack = getStack(0);

            // Burning fuel logic
            if (fuelTime > 0)
                fuelTime--;
            if (fuelTime == 0) {
                dirty = true;

                if (!itemStack.isEmpty() && AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)) {
                    Item itemInitial = itemStack.getItem(); // Before decrementing
                    itemStack.decrement(1);
                    fuelTime = AbstractFurnaceBlockEntity.createFuelTimeMap().get(itemInitial);

                    if (itemStack.isEmpty()) {
                        Item itemRemainder = itemInitial.getRecipeRemainder();
                        setStack(0, itemRemainder == null ? ItemStack.EMPTY : new ItemStack(itemRemainder));
                    }
                }
            }

            // Heat up if burning, cool down if not
            if (fuelTime > 0)
                temperature = Math.min(10, temperature + 0.005f);
            else
                temperature = Math.max(temperature - 0.05f, 0);

            // Burning state toggled, update blockState
            burningFinalState = fuelTime > 0;
            if (burningInitialState != burningFinalState) {
                dirty = true;
                this.world.setBlockState(this.pos,
                        this.world.getBlockState(this.pos).with(FurnaceGenerator.LIT, burningFinalState),
                        3); // Flags 1 | 2 = update block & send changes to client
            }
        }

        if (dirty)
            markDirty();
    }



    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();

        if (normalizedOutgoingNodes.size() == 2) {
            // TODO: resistance in internal circuit
            // TODO: config library for all values
            // TODO: internal resistance
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualVoltageSource(1.0), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }

        return internalCircuit;
    }



    // Hoppers can always insert fuel, but never extract
    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && AbstractFurnaceBlockEntity.canUseAsFuel(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && !AbstractFurnaceBlockEntity.canUseAsFuel(stack);
    }


    // GUI
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BoxScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }
}

