package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualFixedNode;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class MUCPortBlockEntity extends AbstractElectricalBlockEntity {
    private int portId;
    private final VirtualFixedNode fixedNode;

    public MUCPortBlockEntity() {
        super(MUCBlockRegistry.MUC_PORT_ENTITY);
        fixedNode = new VirtualFixedNode(0.0);
    }

    public void setPortId(int portId) {
        this.portId = portId;
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

    @Override
    public MultimeterReading getReading() {
        if (normalizedOutgoingNodes.size() == 0)
            return super.getReading().error();

        ArrayList<Text> text = new ArrayList<>();
        text.add(new LiteralText("Id = 0x" + String.format("%02X", portId)));
        return super.getReading().misc(text).absoluteVoltage(fixedNode.getVoltage());
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
