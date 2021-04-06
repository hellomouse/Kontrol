package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualFixedNode;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

public class MUCPortBlockEntity extends AbstractElectricalBlockEntity {
    private int portId;
    private final VirtualFixedNode fixedNode;

    public MUCPortBlockEntity() {
        super(MUCBlockRegistry.MUC_PORT_ENTITY);
        fixedNode = new VirtualFixedNode(0.0);
    }

    public MUCPortBlockEntity portId(int id) {
        this.portId = id;
        return this;
    }

    public int getPortId() {
        return portId;
    }

    public double getPortVoltage() {
        return fixedNode.getVoltage();
    }

    public void setPortVoltage(double voltage) {
        fixedNode.setVoltage(voltage);
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        for (int outNode : normalizedOutgoingNodes)
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.HIGH_RESISTANCE), -1, outNode);
        internalCircuit.addComponent(fixedNode, -1, -1);
        return internalCircuit;
    }

    public MultimeterReading getReading() {
        if (normalizedOutgoingNodes.size() == 0)
            return super.getReading().error();
        return super.getReading().absoluteVoltage(fixedNode.getVoltage());
    }

    @Override
    public boolean canAttach(Direction dir, BlockEntity otherEntity) {
        if (world == null || world.getBlockState(pos) == null)
            throw new IllegalStateException("Invalid block entity: no block state found");
        return true;
    }

    @Override
    public boolean canStartFloodfill() { return true; }
}
