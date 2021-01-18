package net.hellomouse.kontrol.entity.electrical;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;

import java.util.ArrayList;

public abstract class AbstractElectricalBlockEntity extends BlockEntity implements Tickable {
    private ArrayList<Integer> outgoingNodes = new ArrayList<>();

    public AbstractElectricalBlockEntity(BlockEntityType<?> entity) {
        super(entity);
    }

    public void markRemoved() {
        super.markRemoved();
        System.out.println("DELETING\n");
    }

    @Override
    public void tick() {
        boolean dirty = false;

        if (!this.world.isClient) {
            // Read new state from CircuitComputor
        }

        if (dirty)
            markDirty();
    }
}
