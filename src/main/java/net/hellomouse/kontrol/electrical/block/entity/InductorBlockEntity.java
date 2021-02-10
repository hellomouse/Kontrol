package net.hellomouse.kontrol.electrical.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualInductor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

public class InductorBlockEntity extends AbstractPolarizedElectricalBlockEntity implements BlockEntityClientSerializable {
    protected double inductance = 1.0;

    public InductorBlockEntity() {
        super(ElectricalBlockRegistry.INDUCTOR_BLOCK_ENTITY);
    }

    public void setInductance(double inductance, boolean dirty) {
        this.inductance = inductance;
        if (this.inductance <= 0.0) {
            LogManager.getLogger().warn("Attempted to set inductor at " + getPos() + " to invalid inductance " + inductance);
            this.inductance = CircuitValues.LOW_VOLTAGE_RESISTANCE; // TODO: default values
        }
        if (dirty)
            markDirty();
    }

    public void setInductance(double inductance) {
        setInductance(inductance, true);
    }


    public double getInductance() { return inductance; }

    // TODO: require ticking ture

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();

        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualInductor(inductance), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }

        return internalCircuit;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        setInductance(tag.getDouble("Inductance"), false);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("Inductance", inductance);
        return super.toTag(tag);
    }

    public void fromClientTag(CompoundTag tag) {
        setInductance(tag.getDouble("Inductance"), false);
    }

    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putDouble("Inductance", inductance);
        return tag;
    }

    public MultimeterReading getReading() {
        ArrayList<Text> text = new ArrayList<>();
        text.add(new LiteralText("L = " + inductance));
        return super.getReading().misc(text);
    }
}
