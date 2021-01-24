package net.hellomouse.kontrol.entity.electrical;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualVoltageSource;
import net.hellomouse.kontrol.registry.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;

public class BatteryBlockEntity extends AbstractElectricalBlockEntity {

    // TODO: compute resistance somehow based on temperature

    //Cost Oriented	50	250	60	400
    //Capacity Oriented	12.5	125	240	50
    //Voltage Oriented	200	250	60	6400
    //Current Oriented	50	1000	40	100
    //Life Oriented	50	250	60	400
    //Single-Use	50	500	120	200

    private double internalResistance = 1.0;
    private double energy = 0.0;
    private double voltage = 0.0;

    public BatteryBlockEntity() { super(ElectricalBlockRegistry.BATTERY_BLOCK_ENTITY); }

    /** For use in classes that extend this */
    public BatteryBlockEntity(BlockEntityType<?> entityType) { super(entityType); }

    // TODO: life
    //           double normalisedCurrent = Math.abs(batteryProcess.getDischargeCurrent()) / lifeNominalCurrent;
    //            newLife -= normalisedCurrent * normalisedCurrent * lifeNominalLost * time;


    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();

            internalCircuit.addComponent(new VirtualResistor(internalResistance / 2), normalizedOutgoingNodes.get(0), -1);
            internalCircuit.addComponent(new VirtualVoltageSource(voltage), -1, -2);
            internalCircuit.addComponent(new VirtualResistor(internalResistance / 2), -2, normalizedOutgoingNodes.get(1));
        }
        return internalCircuit;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        internalResistance = tag.getDouble("internal_resistance");
        energy = tag.getDouble("energy");
        voltage = tag.getDouble("voltage");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("internal_resistance", internalResistance);
        tag.putDouble("energy", energy);
        tag.putDouble("voltage", voltage);
        return super.toTag(tag);
    }

    public BatteryBlockEntity internalResistance(double resistance) {
        this.internalResistance = resistance;
        return this;
    }

    public BatteryBlockEntity energy(double energy) {
        this.energy = energy;
        return this;
    }

    public BatteryBlockEntity voltage(double voltage) {
        this.voltage = voltage;
        return this;
    }
}
