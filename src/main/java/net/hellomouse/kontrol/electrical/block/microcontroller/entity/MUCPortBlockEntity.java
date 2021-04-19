package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCPortBlock;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualFixedNode;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IFixedVoltageCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IResistanceCondition;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.items.product_scanner.IProductScanable;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class MUCPortBlockEntity extends AbstractElectricalBlockEntity implements IProductScanable {
    public static final int PORT_ON_BRIGHTNESS = 7;

    private int portId;
    private final VirtualFixedNode fixedNode;

    public MUCPortBlockEntity() {
        super(MUCBlockRegistry.MUC_PORT_ENTITY);
        fixedNode = new VirtualFixedNode(0.0);
    }

    /**
     * Read the current port voltage. Returns 0 V if not connected
     * to a circuit. Will update the internal resistance of the port
     * to be HiZ and disable any voltage output, as well as mark the circuit
     * as dirty if connected.
     * @return Voltage read
     */
    public double getPortVoltage() {
        // Circuit may not be connected at time of reading
        if (this.circuit == null) {
            world.setBlockState(pos, world.getBlockState(pos).with(MUCPortBlock.ON, false));
            return 0.0;
        }

        boolean temp = setIoMode(false);
        if (temp) circuit.markDirty();

        if (nodalVoltages.size() > 0) {
            double voltage = nodalVoltages.get(0);
            world.setBlockState(pos, world.getBlockState(pos).with(MUCPortBlock.ON, Math.abs(voltage) > 0.1));
            return voltage;
        }
        world.setBlockState(pos, world.getBlockState(pos).with(MUCPortBlock.ON, false));
        return fixedNode.getVoltage();
    }

    /**
     * Set the port's output voltage if connected to a circuit. Does nothing
     * if not connected. Will update the internal resistance of the port
     * to be very low and enable voltage output, as well as mark the circuit
     * as dirty if output voltage differs from the previous.
     * @param voltage Voltage to set
     */
    public void setPortVoltage(double voltage) {
        if (this.circuit == null) {
            world.setBlockState(pos, world.getBlockState(pos).with(MUCPortBlock.ON, false));
            return;
        }

        boolean temp = setIoMode(true);
        double orgVoltage = fixedNode.getVoltage();

        world.setBlockState(pos, world.getBlockState(pos).with(MUCPortBlock.ON, Math.abs(voltage) > 0.1));
        fixedNode.setVoltage(voltage);
        if (temp || Math.abs(orgVoltage - voltage) > 0.01) circuit.markDirty();
    }

    /**
     * Updates internal resistances and fixed node hiZ state for a given
     * output / input enable state.
     * @param output Set to output mode? false = input
     * @return Has the output mode changed after calling this?
     */
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

        if (returned && world != null && !world.isClient) {
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, output ?
                    state.with(MUCPortBlock.OUT, true).with(MUCPortBlock.IN, false) :
                    state.with(MUCPortBlock.IN, true).with(MUCPortBlock.OUT, false));
        }

        return returned;
    }

    /** Set the current port ID, should be non-negative */
    public void setPortId(int portId) { this.portId = portId; }

    /** Get the current port id */
    public int getPortId() { return portId; }

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

        if (fixedNode.isHiZ() && nodalVoltages.size() > 0)
            return super.getReading().misc(text).absoluteVoltage(nodalVoltages.get(0));
        return super.getReading().misc(text).absoluteVoltage(fixedNode.getVoltage());
    }

    @Override
    public ArrayList<Text> productInfo() {
        ArrayList<Text> returned = new ArrayList<>();
        returned.add(new TranslatableText("block.kontrol.muc_port").formatted(Formatting.BOLD));

        String dataStr = "ID = " + portId + " (0x" + String.format("%02X", portId) + ")" + Formatting.GRAY + "  |  ";
        BlockState state = world.getBlockState(pos);

        if (state.get(MUCPortBlock.IN))       dataStr += new TranslatableText("block.kontrol.muc_port.in").getString();
        else if (state.get(MUCPortBlock.OUT)) dataStr += new TranslatableText("block.kontrol.muc_port.out").getString();
        else                                  dataStr += new TranslatableText("block.kontrol.muc_port.disable").getString();

        dataStr += Formatting.GRAY + "  |  " + (state.get(MUCPortBlock.ON) ?
                new TranslatableText("block.kontrol.muc_port.on").getString() :
                new TranslatableText("block.kontrol.muc_port.off").getString());

        returned.add(new LiteralText(dataStr));
        return returned;
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
