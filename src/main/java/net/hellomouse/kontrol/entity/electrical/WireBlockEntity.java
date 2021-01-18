package net.hellomouse.kontrol.entity.electrical;

import net.hellomouse.kontrol.registry.ElectricalBlockRegistry;

public class WireBlockEntity extends AbstractElectricalBlockEntity {
    public WireBlockEntity() {
        super(ElectricalBlockRegistry.WIRE_BLOCK_ENTITY);
    }
}
