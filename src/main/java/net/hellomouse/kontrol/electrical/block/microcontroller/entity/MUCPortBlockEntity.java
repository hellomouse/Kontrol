package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualFixedNode;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IFixedVoltageCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IResistanceCondition;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
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

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.portId = tag.getInt("portId");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("portId", portId);
        return super.toTag(tag);
    }

    public double getPortVoltage() {
        if (this.circuit == null) return 0.0; // TODO: why is this null

        boolean temp = setIoMode(false);
        if (temp) circuit.markDirty();

        if (nodalVoltages.size() > 0)
            return nodalVoltages.get(0);
        return fixedNode.getVoltage();
    }

    public void setPortVoltage(double voltage) {
        if (this.circuit == null) return;

        boolean temp = setIoMode(true);
        double orgVoltage = fixedNode.getVoltage();

        fixedNode.setVoltage(voltage);
        if (temp || Math.abs(orgVoltage - voltage) > 0.01) circuit.markDirty();
    }

    private boolean setIoMode(boolean output) {
        // Input: Inf R, hiZ
        // Output: 0 R, not hi Z
        double resistance = output ? CircuitValues.LOW_RESISTANCE : CircuitValues.PORT_RESISTANCE;
        boolean returned = fixedNode.isHiZ() == !output;

        for (AbstractVirtualComponent component : internalCircuit.getComponents()) {
            if (component instanceof IResistanceCondition)
                ((IResistanceCondition) component).setResistance(resistance);
            else if (component instanceof IFixedVoltageCondition)
                component.setHiZ(!output);
        }
        return returned;
    }

    @Override
    public boolean canConnectTo(Direction dir, BlockEntity otherEntity) {
        if (otherEntity instanceof MUCPortBlockEntity)
            return false;
        return super.canConnectTo(dir, otherEntity);
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        for (int outNode : normalizedOutgoingNodes)
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.PORT_RESISTANCE), -1, outNode);
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
