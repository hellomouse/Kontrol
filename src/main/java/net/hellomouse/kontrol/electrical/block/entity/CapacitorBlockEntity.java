package net.hellomouse.kontrol.electrical.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualCapacitor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;


public class CapacitorBlockEntity extends AbstractPolarizedElectricalBlockEntity implements BlockEntityClientSerializable {
    protected double capacitance = 1.0;

    public CapacitorBlockEntity() {
        super(ElectricalBlockRegistry.CAPACITOR_BLOCK_ENTITY);
    }

    public void setCapacitance(double capacitance, boolean dirty) {
        this.capacitance = capacitance;
        if (this.capacitance <= 0.0) {
            LogManager.getLogger().warn("Attempted to set capacitor at " + getPos() + " to invalid capacitance " + capacitance);
            this.capacitance = CircuitValues.LOW_VOLTAGE_RESISTANCE; // TODO: default values
        }
        if (dirty)
            markDirty();
    }

    public void setCapacitance(double capacitance) {
        setCapacitance(capacitance, true);
    }


    public double getCapacitance() { return capacitance; }

    // TODO: require ticking ture

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();

        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualCapacitor(capacitance), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }

        return internalCircuit;
    }

    @Override
    public boolean recomputeEveryTick() { return true; }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        setCapacitance(tag.getDouble("Capacitance"), false);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("Capacitance", capacitance);
        return super.toTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        setCapacitance(tag.getDouble("Capacitance"), false);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putDouble("Capacitance", capacitance);
        return tag;
    }

    @Override
    public MultimeterReading getReading() {
        ArrayList<Text> text = new ArrayList<>();
        text.add(new LiteralText("C = " + capacitance));
        return super.getReading().misc(text);
    }

    @Override
    public boolean canStartFloodfill() { return true; }
}
