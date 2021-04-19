package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCRedstonePortBlockBOG07;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class MUCRedstonePortBlockEntity extends AbstractElectricalBlockEntity {
    private double lowThreshold = 0.0;

    public MUCRedstonePortBlockEntity() {
        super(MUCBlockRegistry.MUC_REDSTONE_PORT_ENTITY);
    }

    /**
     * Set the threshold voltage to enable redstone output
     * @param lowThreshold Low threshold to activate
     * @return this
     */
    public MUCRedstonePortBlockEntity lowThreshold(double lowThreshold) {
        this.lowThreshold = lowThreshold;
        return this;
    }

    /**
     * Returns measured voltage for voltage->redstone function.
     * Returns 0.0 V when not connected
     * @return Voltage
     */
    public double getVoltage() {
        if (circuit == null || internalCircuit.getComponents().size() == 0)
            return 0.0;
        return circuit.virtualCircuit().getNodalVoltage(internalCircuit.getComponents().get(0).getNode2());
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient) return;
        BlockState blockState = world.getBlockState(pos);
        world.setBlockState(pos, blockState.with(MUCRedstonePortBlockBOG07.POWERING, getVoltage() > lowThreshold));
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        for (int outNode : normalizedOutgoingNodes)
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.PORT_RESISTANCE), -1, outNode);
        return internalCircuit;
    }

    @Override
    public MultimeterReading getReading() {
        if (normalizedOutgoingNodes.size() == 0)
            return super.getReading().error();
        return super.getReading().absoluteVoltage(getVoltage());
    }

    @Override
    public boolean canAttach(Direction dir, BlockEntity otherEntity) {
        if (world == null || world.getBlockState(pos) == null)
            throw new IllegalStateException("Invalid block entity: no block state found");
        return world.getBlockState(pos).get(Properties.FACING) == dir;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.lowThreshold = tag.getDouble("lowThreshold");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("lowThreshold", lowThreshold);
        return super.toTag(tag);
    }
}
