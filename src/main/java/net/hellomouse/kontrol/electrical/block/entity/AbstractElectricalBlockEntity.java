package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.IHasCircuitManager;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.circuit.Circuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;


/**
 * Block entity for an electrical block
 * @author Bowserinator
 */
public abstract class AbstractElectricalBlockEntity extends BlockEntity implements Tickable {
    // Node ids assigned to each face as a temporary step. Not normalized, nor will all node ids be used
    // Use normalizedOutgoingNodes unless you know what you're doing
    protected ArrayList<Integer> outgoingNodes = new ArrayList<>();
    // True if connected to another electrical component on the side. Array matches order of Circuit.indexFromDirection
    protected ArrayList<Boolean> connectedSides = new ArrayList<>(Arrays.asList(false, false, false, false, false, false));
    // Already computed connected sides?
    private boolean computedConnectedSides = false;

    // Proper, normalized node ids, array length = # of connected sides
    protected ArrayList<Integer> normalizedOutgoingNodes = new ArrayList<>();
    // Maps normalized node ID => Direction
    protected HashMap<Integer, Direction> normalizedNodeToDir = new HashMap<>();
    // Nodal voltages, maps iteration order for normalizedOutgoingNodes
    protected ArrayList<Double> nodalVoltages = new ArrayList<>();

    // Internal circuit, will have new node ids assigned when added to circuit
    protected VirtualCircuit internalCircuit = new VirtualCircuit();
    // Circuit it belongs to
    protected Circuit circuit;


    private UUID savedCircuitUUID = null;

    // TODO: save these to tags
    protected double current, voltage, power;

    // Thermal simulation
    protected double temperature;
    protected double thermalR = 0.0;
    protected double thermalC = 0.0;
    protected double tAmbientOffset = 0.0;

    private double tAmbient = 0.0;
    private double heatDissipationRate = 0.0;
    private boolean temperatureSetYet = false;


    /**
     * Construct an AbstractElectricalBlockEntity
     * @param entity Block entity type
     */
    public AbstractElectricalBlockEntity(BlockEntityType<?> entity) {
        super(entity);
    }


    // ----- Thermal simulation ----- \\


    public void updateAmbientTemperature() {
        tAmbientOffset = 0.0;
        for (Direction dir : Direction.values()) {
            Block block = world.getBlockState(pos.offset(dir)).getBlock();
            if (block == Blocks.LAVA)
                tAmbientOffset += 100.0;
        }
        tAmbient = 13.6484805403 * world.getBiome(pos).getTemperature(pos) + 7.0879687222 + tAmbientOffset;
    }

    public void thermalSim() {
        // https://www.reddit.com/r/Minecraft/comments/3eh7yu/the_rl_temperature_of_minecraft_biomes_revealed/
        // TODO document

        if (!temperatureSetYet) {
            temperatureSetYet = true;
            updateAmbientTemperature();
            temperature = tAmbient;
        }


        thermalC = 100.0;
        thermalR = 1.0;

        // Dissipates heat instantly TODO
        if (thermalC == 0.0 && thermalR == 0.0) {
            temperature = tAmbient;
            return;
        }

        // dT / dt * thermal capacitance = power disappation rate

        // V = getPower() * thermalR
        // V -- VVVVV --- ||
        // Voltage across capacitor = temp
        // i = dV / dt * C
        // integral power / C = T_diff

        // TODO: override a computePower() abstract

        double thermalSource = getPower() * thermalR + tAmbient;

        double oldTemp = temperature;
        temperature += 1 / thermalC * heatDissipationRate;
        heatDissipationRate = (thermalSource - temperature) / thermalR;

        // Divergence check
        if ((oldTemp < temperature && temperature > thermalSource) ||
                (oldTemp > temperature && temperature < thermalSource)) {
            heatDissipationRate = 0.0;
            temperature = thermalSource;
        }
    }

    public double getPower() {
        try {
            if (internalCircuit.getComponents().size() > 0)
                return (internalCircuit.getComponents().get(0).getPower());
            return 0.0;
        }
        catch(Exception e) {
            return 0.0;
        }
    }



    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (circuit != null)
            savedCircuitUUID = circuit.id;
        if (savedCircuitUUID != null)
            tag.putUuid("CircuitUUID", savedCircuitUUID);
        return super.toTag(tag);
    }


    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        if (tag.contains("CircuitUUID"))
            savedCircuitUUID = tag.getUuid("CircuitUUID");
    }



    // TODO: update slowly or update quickly?

    public void onUpdate() {

    }

    public boolean recomputeEveryTick() { return false; }


    /**
     * Returns the internal circuit representation of this block entity
     * @return VirtualCircuit representing the internal circuit. Outgoing nodes should be numbered
     *         from 0, 1, 2... while internal nodes that cannot connect with other components
     *         should be numbered -1, -2, ...
     */
    public abstract VirtualCircuit getInternalCircuit();

    /**
     * Preliminary outgoing node generation. All 6 possible outgoing nodes (1 for
     * each face of a cube) is assigned a unique ID in Circuit, and nodes are replaced
     * for connected components.
     *
     * @param offset Unique offset for generating all 6 outgoing node IDs
     */
    public void generatePreliminaryOutgoingNodes(int offset) {
        offset *= 6; // Avoid duplication of any of the 6 faces
        outgoingNodes = new ArrayList<>(Arrays.asList(offset, offset + 1, offset + 2, offset + 3, offset + 4, offset + 5));
    }

    public abstract boolean canAttach(Direction dir, BlockEntity otherEntity);

    public boolean canConnectTo(Direction dir, BlockEntity otherEntity) {
        if (!(otherEntity instanceof AbstractElectricalBlockEntity))
            return false;
        return this.canAttach(dir, otherEntity) &&
                ((AbstractElectricalBlockEntity)otherEntity).canAttach(dir.getOpposite(), this);
    }

    public void flagRecomputeConnectedSides() {
        computedConnectedSides = false;
    }


    @Override
    public void tick() {
        boolean dirty = false;

        if (world != null && !world.isClient) {
            // Force computation of sides next tick
            // TODO: why?
           // computedConnectedSides = false;

            thermalSim();

            // ALL THIS TODO
            // If not part of a circuit initiate floodfill from Circuitmanager

            if (circuit == null) {
                // Check neighbors for non-null components

                boolean found = false;
                for (Direction dir : Direction.values()) {
                    BlockEntity _otherEntity = world.getBlockEntity(pos.offset(dir));

                    if (_otherEntity instanceof AbstractElectricalBlockEntity) {
                        AbstractElectricalBlockEntity otherEntity = (AbstractElectricalBlockEntity)_otherEntity;

                        if (canConnectTo(dir, otherEntity) && otherEntity.canConnectTo(dir.getOpposite(), this)) {
                            AbstractElectricalBlockEntity e2 = ((AbstractElectricalBlockEntity)world.getBlockEntity(pos.offset(dir)));
                            Circuit c = e2.getCircuit();
                            if (c != null && !e2.isRemoved() && !c.isDeleted()) {
                                found = true;
                                c.flagElementAdded(e2.getPos());
                                // System.out.println("Found connecting circuit, flaging as invalid");
                                break;
                            }
                        }
                    }
                }

                if (!found && canStartFloodfill()) {
                    circuit = new Circuit((ServerWorld)world, pos, UUID.randomUUID()); // savedCircuitUUID == null ? UUID.randomUUID() : savedCircuitUUID
                    circuit = ((IHasCircuitManager)world).getCircuitManager().addCircuit(circuit);
                    //if (circuit != null)
                    //    System.out.println("Generating circuit for " + this.getPos());
                }
            }

            // TODO: dont run literally every tick, check if circuit actually reconstructed?
            onUpdate();
            // dirty = true;

            // cirucitmanager.floodFill(this pos, this, etc...) // also set circuit TODO

            // flag calculation if necessary
            if (recomputeEveryTick() && this.circuit != null)
                circuit.markDirty();

            // voltage = ... todo get state somehow ibstead
        }

        if (dirty)
            markDirty();
    }

    public void markRemoved() {
        super.markRemoved();
        if (circuit != null)
            circuit.flagElementRemoved(pos);
    }



    public void clearVoltages() {
        nodalVoltages.clear();
    }

    public void setVoltage(int node, double voltage) {
        if (nodalVoltages.size() < normalizedOutgoingNodes.size())
            for (int i = 0; i < normalizedOutgoingNodes.size(); i++)
                nodalVoltages.add(0.0);
        nodalVoltages.set(normalizedOutgoingNodes.indexOf(node), voltage);
    }


    // --- Connection computation --- \\
    public ArrayList<Integer> getOutgoingNodes() {
        return outgoingNodes;
    }

    public ArrayList<Boolean> getConnectedSides() {
        recomputeSidesIfNecessary();
        return connectedSides;
    }

    public ArrayList<Integer> getNormalizedOutgoingNodes() {
        recomputeSidesIfNecessary();
        return normalizedOutgoingNodes;
    }

    private void recomputeSidesIfNecessary() {
        if (!computedConnectedSides)
            computeConnectedSides();
        computedConnectedSides = true;
    }

    /**
     * Reset and populate connectedSides and normalizedOutgoingNodes
     * from world data.
     */
    public void computeConnectedSides() {
        connectedSides.clear();
        normalizedOutgoingNodes.clear();

        for (Direction dir : Direction.values()) {
            boolean canConnect = this.canConnectTo(dir, world.getBlockEntity(pos.offset(dir)));

            connectedSides.add(canConnect);

            // TODO: why isUSperconducting(0
            if (canConnect && !isSuperconducting())
                normalizedOutgoingNodes.add(outgoingNodes.get(Circuit.indexFromDirection(dir)));
        }
    }

    /**
     * Outgoing nodes will be sorted so the 0th index contains
     * the positive node and the 1st index contains the negative
     */
    protected void sortOutgoingNodesByPolarity() {
        // Do nothing since not polarized block entity
    }

    public void clearConnectedSides() {
        outgoingNodes.clear();
        connectedSides = new ArrayList<>(Arrays.asList(false, false, false, false, false, false));
    }

    public void setOutgoingNode(Direction dir, int nodeId){
        outgoingNodes.set(Circuit.indexFromDirection(dir), nodeId);
    }

    public void setNormalizedOutgoingNode(int nodeId, Direction dir) {
        normalizedNodeToDir.put(nodeId, dir);
    }

    public boolean canStartFloodfill() {
        return false;
    }

    public boolean isSuperconducting() {
        return false;
    }

    // --- World interactions --- \\
    public MultimeterReading getReading() {
        return new MultimeterReading()
                .nodeIds(normalizedOutgoingNodes)
                .nodalVoltages(nodalVoltages)
                .blockType(this.getClass().toString().split(" ")[1])
                .temperature(temperature);
    }


    // --- Circuit getters / setters --- \\
    public void setCircuit(Circuit c) { circuit = c; }
    public Circuit getCircuit() { return circuit; }
}
