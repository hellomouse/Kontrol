package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.minecraft.block.entity.BlockEntityType;

public abstract class AbstractPolarizedElectricalBlockEntity extends AbstractElectricalBlockEntity {
    public AbstractPolarizedElectricalBlockEntity(BlockEntityType<?> entityType) {
        super(entityType);
    }

    public MultimeterReading getReading() {
        if (nodalVoltages.size() < 2)
            return super.getReading().error();

        // Doesn't use internal component because can have internal resistors
        double voltage = nodalVoltages.get(0) - nodalVoltages.get(1);
        double current = internalCircuit.getComponents().get(0).getCurrent();
        return super.getReading()
                .voltage(voltage)
                .current(current)
                .power(Math.abs(voltage * current))
                .polarity(normalizedNodeToDir.get(normalizedOutgoingNodes.get(0)),
                          normalizedNodeToDir.get(normalizedOutgoingNodes.get(1)));
    }
}
