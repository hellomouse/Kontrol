package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.entity.BlockEntityType;

public class SuperconductingWireBlockEntity extends WireBlockEntity {
    public SuperconductingWireBlockEntity() {
        super(ElectricalBlockRegistry.SUPERCONDUCTING_WIRE_BLOCK_ENTITY);
    }

    /**
     * For use in classes that extend this
     */
    protected SuperconductingWireBlockEntity(BlockEntityType<?> entityType) {
        super(entityType);
    }

    public boolean isSuperconducting() {
        return true;
    }

    @Override
    public SuperconductingWireBlockEntity color(ColorData.COLOR_STRING color) {
        this.color = color;
        return this;
    }

    @Override
    public MultimeterReading getReading() {
        return new MultimeterReading().current(1.0f);
    }
}

