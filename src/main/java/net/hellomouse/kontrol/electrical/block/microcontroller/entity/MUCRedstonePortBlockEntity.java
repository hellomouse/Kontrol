package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCRedstonePortBlockBOG07;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.items.product_scanner.IProductScanable;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class MUCRedstonePortBlockEntity extends AbstractElectricalBlockEntity implements IProductScanable {
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
    public ArrayList<Text> productInfo() {
        ArrayList<Text> returned = new ArrayList<>();

        // TODO: save what model?
        returned.add(new TranslatableText("block.kontrol.muc_redstone_port_1").formatted(Formatting.BOLD));
        returned.add(new LiteralText("V_on = " + lowThreshold + " V " + Formatting.GRAY + "  |  " + Formatting.RED + "Rs = " + (getVoltage() > lowThreshold ? "15" : "0")));
        return returned;
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
