package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualDiode;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;


public class DiodeBlockEntity extends AbstractPolarizedElectricalBlockEntity {
    protected double forwardVoltage = 1.0;

    public DiodeBlockEntity() { super(ElectricalBlockRegistry.DIODE_BLOCK_ENTITY); }

    /** For use in classes that extend this */
    public DiodeBlockEntity(BlockEntityType<?> entityType) { super(entityType); }

    /**
     * Sets forward voltage and returns this. Lets you use a pattern like this:
     * return new DiodeBlockEntity().forwardVoltage(voltage);
     * @param voltage Forward voltage
     * @return this
     */
    public DiodeBlockEntity forwardVoltage(double voltage) {
        setForwardVoltage(voltage);
        return this;
    }

    public void setForwardVoltage(double forwardVoltage, boolean dirty) {
        this.forwardVoltage = forwardVoltage;
        if (this.forwardVoltage <= 0.0) {
            LogManager.getLogger().warn("Attempted to set diode at " + getPos() + " to invalid forward voltage " + forwardVoltage);
            this.forwardVoltage = 0.0;
        }

        if (dirty) {
            if (nodalVoltages.size() == 2)
                ((VirtualDiode)internalCircuit.getComponents().get(1)).setVForward(forwardVoltage);
            markDirty();
        }
    }

    public void setForwardVoltage(double forwardVoltage) { setForwardVoltage(forwardVoltage, true); }

    public double getForwardVoltage() { return forwardVoltage; }

    @Override
    public void onUpdate() {
        if (canSafelyMeasureCircuit()) {
            VirtualDiode diode = (VirtualDiode)internalCircuit.getComponents().get(1);
            if (diode.shouldBeHiZ() != diode.isHiZ())
                circuit.markDirty();
        }
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.LOW_RESISTANCE), normalizedOutgoingNodes.get(0), -1);
            internalCircuit.addComponent(new VirtualDiode(forwardVoltage), -1, -2);
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.LOW_RESISTANCE), -2, normalizedOutgoingNodes.get(1));
        }
        return internalCircuit;
    }

    @Override
    public MultimeterReading getReading() {
        ArrayList<Text> text = new ArrayList<>();
        text.add(new LiteralText("V_fwd = " + forwardVoltage + " | ON = " + !internalCircuit.getComponents().get(1).isHiZ() ));
        return super.getReading().misc(text);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        setForwardVoltage(tag.getDouble("ForwardVoltage"), false);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("ForwardVoltage", forwardVoltage);
        return super.toTag(tag);
    }
}
