package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.AbstractElectricalBlock;
import net.hellomouse.kontrol.electrical.block.interfaces.IPolarizedBlock;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.circuit.Circuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Block entity for an electrical block
 */
public abstract class AbstractElectricalBlockEntity extends BlockEntity implements Tickable {
    protected ArrayList<Integer> outgoingNodes = new ArrayList<>();

    protected ArrayList<Integer> normalizedOutgoingNodes = new ArrayList<>();
    protected HashMap<Integer, Direction> normalizedNodeToDir = new HashMap<>();
    protected ArrayList<Double> nodalVoltages = new ArrayList<>();

    protected VirtualCircuit internalCircuit = new VirtualCircuit();

    protected Circuit circuit;

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
     * Reset and populate connectedSides and normalizedOutgoingNodes
     * from world data.
     */
    public void computeConnectedSides() {
        connectedSides.clear();
        normalizedOutgoingNodes.clear();

        for (Direction dir : Direction.values()) {
            BlockState state = world.getBlockState(pos);
            BlockState otherState = world.getBlockState(pos.offset(dir));
            AbstractElectricalBlock ownBlock = (AbstractElectricalBlock)state.getBlock();
            boolean canConnect = ownBlock.canConnect(state, dir, otherState);

            connectedSides.add(canConnect);
            if (canConnect)
                normalizedOutgoingNodes.add(outgoingNodes.get(Circuit.indexFromDirection(dir)));
        }
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

        if (circuit != null) circuit.markInvalid();
    }

    public void toTag() {
        // idk read state from the thingy
    }


    // TODO: update slowly or update quickly?

    public void onUpdate() {

    }

    private boolean recomputeEveryTick() { return false; }



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
                System.out.println("Generating circuit for " + this.getPos());
                circuit = new Circuit(world);
            }
            circuit.verifyIntegrity(this.world, this.getPos());

            // TODO: dont run literally every tick, check if circuit actually reconstructed?
            onUpdate();
            // dirty = true;

            // cirucitmanager.floodFill(this pos, this, etc...) // also set circuit TODO

            // flag calculation if necessary
           //  if (recomputeEveryTick())
            //    ElectricalBlockRegistry.circuitManager.doComputation();

            // Read new state from CircuitComputor
            // current = ElectricalBlockRegistry.circuitManager.computeComponentVoltage(this);
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
     * Outgoing nodes will be sorted so the 0th index contains
     * the positive node and the 1st index contains the negative
     */
    protected void sortOutgoingNodesByPolarity() {
        BlockState state = world.getBlockState(pos);
        AbstractElectricalBlock block = (AbstractElectricalBlock)state.getBlock();

        if (block instanceof IPolarizedBlock) {
            int node1 = normalizedOutgoingNodes.get(0);
            int node2 = normalizedOutgoingNodes.get(1);

            if (normalizedNodeToDir.get(node1) != ((IPolarizedBlock) block).positiveTerminal(state)) {
                normalizedOutgoingNodes.set(0, node2);
                normalizedOutgoingNodes.set(1, node1);
            }
        }
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
