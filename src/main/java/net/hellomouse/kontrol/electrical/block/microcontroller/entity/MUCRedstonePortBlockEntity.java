package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCRedstonePortBlock;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import java.util.function.Function;

public class MUCRedstonePortBlockEntity extends AbstractElectricalBlockEntity {
    private Function<Double, Boolean> voltageToRedstoneFunction = voltage -> voltage != 0.0;

    public MUCRedstonePortBlockEntity() {
        super(MUCBlockRegistry.MUC_REDSTONE_PORT_ENTITY);
    }

    /**
     * Set the function to convert voltage -> redstone power, default non-zero voltage = 15 else 0
     * @param voltageToRedstoneFunction Function, accepts double as voltage, outputs boolean whether to power
     * @return this
     */
    public MUCRedstonePortBlockEntity voltageToRedstoneFunction(Function<Double, Boolean> voltageToRedstoneFunction) {
        this.voltageToRedstoneFunction = voltageToRedstoneFunction;
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
        world.setBlockState(pos, blockState.with(MUCRedstonePortBlock.POWERING, voltageToRedstoneFunction.apply(getVoltage())));
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
}
