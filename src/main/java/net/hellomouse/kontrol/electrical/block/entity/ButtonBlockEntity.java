package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.BasicPushButtonBlock;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;

public class ButtonBlockEntity extends ResistorBlockEntity {
    private int tickCooldown = 0;

    public ButtonBlockEntity() {
        super(ElectricalBlockRegistry.BUTTON_BLOCK_ENTITY);
        setRotate(true);
    }

    @Override
    public void onUpdate() {
        if (tickCooldown > 0) {
            tickCooldown--;
            if (tickCooldown == 0) {
                setResistance(1e9);
                world.setBlockState(pos, world.getBlockState(pos).with(BasicPushButtonBlock.PRESSED, false));
                if (circuit != null)
                    circuit.markDirty();
            }
        }
    }

    public void press() {
        if (world != null && world.isClient) return;
        boolean shouldMarkDirty = tickCooldown == 0;
        tickCooldown = 15;
        setResistance(1.0);
        world.setBlockState(pos, world.getBlockState(pos).with(BasicPushButtonBlock.PRESSED, true));
        if (this.circuit != null && shouldMarkDirty) {
            circuit.markDirty();
        }
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
