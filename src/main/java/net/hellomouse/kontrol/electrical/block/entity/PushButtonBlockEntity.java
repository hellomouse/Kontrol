package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.BasicPushButtonBlock;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;


/**
 * Push button block entity.
 * @author Bowserinator
 */
public class PushButtonBlockEntity extends ResistorBlockEntity {
    private int tickCooldown = 0;
    private int pushTime = CircuitValues.DEFAULT_PUSH_BUTTON_PUSH_TIME;

    public PushButtonBlockEntity() {
        super(ElectricalBlockRegistry.PUSH_BUTTON_BLOCK_ENTITY);
        setRotate(true);
    }

    public PushButtonBlockEntity pushTime(int pushTime) {
        this.pushTime = pushTime;
        return this;
    }

    @Override
    public void onUpdate() {
        if (tickCooldown > 0) {
            tickCooldown--;
            if (tickCooldown == 0) {
                setResistance(CircuitValues.HIGH_RESISTANCE);
                if (world != null)
                    world.setBlockState(pos, world.getBlockState(pos).with(BasicPushButtonBlock.PRESSED, false));
                if (circuit != null)
                    circuit.markDirty();
            }
        }
    }

    public void press() {
        if (world != null && world.isClient) return;

        // If already down when pressed no state change occurs, only refresh the tickCooldown
        boolean shouldMarkDirty = tickCooldown == 0;

        tickCooldown = pushTime;
        setResistance(CircuitValues.LOW_RESISTANCE);
        world.setBlockState(pos, world.getBlockState(pos).with(BasicPushButtonBlock.PRESSED, true));

        if (this.circuit != null && shouldMarkDirty)
            circuit.markDirty();
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        tickCooldown = tag.getInt("tickCooldown");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("tickCooldown", tickCooldown);
        return super.toTag(tag);
    }
}
