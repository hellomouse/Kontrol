package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.IHasCircuitManager;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.circuit.Circuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
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
    protected ArrayList<Integer> outgoingNodes = new ArrayList<>();

    protected ArrayList<Integer> normalizedOutgoingNodes = new ArrayList<>();
    protected HashMap<Integer, Direction> normalizedNodeToDir = new HashMap<>();
    protected ArrayList<Double> nodalVoltages = new ArrayList<>();

    protected VirtualCircuit internalCircuit = new VirtualCircuit();

    protected Circuit circuit;

    private UUID savedCircuitUUID = null;

    // TODO: save these to tags
    protected double current, voltage, power;
    protected double temperature;

    // True if connected on the side. Array matches order of iteration of Direction.values()
    protected ArrayList<Boolean> connectedSides = new ArrayList<>(Arrays.asList(false, false, false, false, false, false));

    private boolean computedConnectedSides = false;

    /**
     * Construct an AbstractElectricalBlockEntity
     * @param entity Block entity type
     */
    public AbstractElectricalBlockEntity(BlockEntityType<?> entity) {
        super(entity);
    }

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


    public void markRemoved() {
        super.markRemoved();
        System.out.println("DELETING\n");

        if (circuit != null) circuit.flagElementRemoved(pos);
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


    public abstract boolean canAttach(Direction dir, BlockEntity otherEntity);

    public boolean canConnectTo(Direction dir, BlockEntity otherEntity) {
        if (!(otherEntity instanceof AbstractElectricalBlockEntity))
            return false;
        return this.canAttach(dir, otherEntity) &&
                ((AbstractElectricalBlockEntity)otherEntity).canAttach(dir.getOpposite(), this);
    }



    @Override
    public void tick() {
        boolean dirty = false;

        if (!world.isClient) {
            // Force computation of sides next tick
            // TODO: why?
            computedConnectedSides = false;


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
                            AbstractElectricalBlockEntity e2  =((AbstractElectricalBlockEntity)world.getBlockEntity(pos.offset(dir)));
                            Circuit c = e2.getCircuit();
                            if (c != null && !e2.isRemoved()) {
                                found = true;
                                c.flagElementAdded(e2.getPos());
                                // System.out.println("Found connecting circuit, flaging as invalid");
                                break;
                            }
                        }
                    }
                }

                if (!found && canStartFloodfill()) {
                    System.out.println(world.hashCode() + " WORLD!!");
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
            if (canConnect)
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
