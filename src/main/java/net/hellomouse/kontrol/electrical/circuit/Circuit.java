package net.hellomouse.kontrol.electrical.circuit;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;


/**
 * A physical Minecraft circuit, internally solved with a VirtualCircuit.
 * Handles blocks being deleted and added to a network.
 * @author Bowserinator
 */
public class Circuit {
    // Unique identifier for the circuit, can be saved
    public final UUID id;

    // Circuit requires re-solve? (not reconstruction, overridden by invalid)
    private boolean dirty = true;
    // Circuit requires re-floodfilling?
    private boolean invalid = true;
    // Has a component been removed? Set to true with invalid,
    // used for floodfill optimizations
    private boolean componentBeenRemoved = false;
    // Circuit is scheduled to be deleted?
    private boolean deleted = false;
    // Location to start floodfill, must be a location within the circuit
    // (As in an AbstractElectricalBlockEntity occupies the location)
    private BlockPos floodfillPos;

    private final ArrayList<AbstractElectricalBlockEntity> blockEntities = new ArrayList<>();
    private final World world;

    private final VirtualCircuit circuit = new VirtualCircuit();

    /**
     * Construct a new circuit. Should be called from an AbstractElectricalBlockEntity.
     * Note: you must add the circuit to the world circuit manager for it to do anything,
     * not doing so may result in invalid circuit states!
     *
     * <pre>{@code
     * circuit = ((IHasCircuitManager)world).getCircuitManager().addCircuit(circuit);
     * }</pre>
     *
     * @see CircuitManager
     * @param world Server world the block entity belongs to
     * @param pos Position of the block entity (tentative location to begin floodfill)
     * @param id Unique UUID of the circuit
     */
    public Circuit(ServerWorld world, BlockPos pos, UUID id) {
        this.world = world;
        this.floodfillPos = pos;
        this.id = id;
    }

    /**
     * Called by CircuitManager at the end of every tick.
     * Checks for any changes and solves / re-constructs if required
     */
    public void postTick() {
        // Scheduled to be deleted, don't do any solving
        if (deleted) return;

        // Circuit structure has been invalidated, re-do floodfill and
        // virtual circuit construction
        if (invalid) {

            // If an element was removed, select a start floodfill location
            // at any existing block entity (otherwise floodfill pos was set
            // when component was added)
            if (componentBeenRemoved) {
                floodfillPos = null;
                for (AbstractElectricalBlockEntity blockEntity : blockEntities) {
                    blockEntity.setCircuit(null);
                    if (floodfillPos == null && !blockEntity.isRemoved())
                        floodfillPos = blockEntity.getPos();
                }

                // No more elements in circuit
                if (floodfillPos == null) {
                    ((IHasCircuitManager)world).getCircuitManager().deleteCircuit(this);
                    return;
                }
            }

            floodFill(floodfillPos);

            floodfillPos = null;
            invalid = false;
            dirty = false;
            componentBeenRemoved = false;
        }

        // Circuit only needs to be re-solved
        else if (dirty) {
            circuit.tick();
            solve();
            dirty = false;
        }
    }

    /**
     * Perform a floodfill starting at a location
     * Can behave differently depending on if componentBeenRemoved is true
     * @param pos Location to start floodfill
     */
    private void floodFill(BlockPos pos) {
        circuit.clear();

        Queue<BlockPos> posToVisit = new LinkedList<>();
        posToVisit.add(pos);

        for (AbstractElectricalBlockEntity e : blockEntities) {
            e.clearConnectedSides();
            e.setCircuit(null);
            e.flagRecomputeConnectedSides();
        }
        blockEntities.clear();

        int index = 0; // Used for generating offset of preliminary node ids
        while (posToVisit.size() > 0) {
            BlockPos p = posToVisit.remove();
            BlockEntity entity = world.getBlockEntity(p);

            if (!(entity instanceof AbstractElectricalBlockEntity))
                break;

            AbstractElectricalBlockEntity electricalEntity = ((AbstractElectricalBlockEntity) entity);

            if (electricalEntity.isSuperconducting())
                continue;

            electricalEntity.setCircuit(this);
            blockEntities.add(electricalEntity);

            if (electricalEntity.getOutgoingNodes().size() == 0)
                electricalEntity.generatePreliminaryOutgoingNodes(index);

            for (Direction dir : Direction.values()) {
                BlockEntity newEntity = world.getBlockEntity(p.offset(dir));
                if (!(newEntity instanceof AbstractElectricalBlockEntity))
                    continue;

                AbstractElectricalBlockEntity eEntity = ((AbstractElectricalBlockEntity) newEntity);
                if (!(electricalEntity.getConnectedSides().get(indexFromDirection(dir)))) // No valid connection, checked in the entity
                    continue;

                if (eEntity.isSuperconducting()) {
                    index = superconductorFloodfill(p.offset(dir), posToVisit, electricalEntity.getOutgoingNodes().get(indexFromDirection(dir)), index);
                    continue;
                }

                // We already traversed this entity, add new connection
                if (eEntity.getOutgoingNodes().size() > 0 && electricalEntity.getOutgoingNodes().size() > 0 && eEntity.getCircuit() == this) {
                    electricalEntity.setOutgoingNode(dir, eEntity.getOutgoingNodes().get(indexFromDirection(dir.getOpposite())));
                }

                // New entity
                if (eEntity.getCircuit() != this) {
                    if (eEntity.getCircuit() != null && !eEntity.getCircuit().id.equals(id))
                        ((IHasCircuitManager)world).getCircuitManager().deleteCircuit(eEntity.getCircuit());

                    posToVisit.add(p.offset(dir));
                    eEntity.setCircuit(this);
                }
            }
            index++;
        }

        Direction[] directions = Direction.values();
        Map<Integer, Integer> nodeReductionMap = new HashMap<>();
        int currentNodeID = 0;

        // Perform node ID normalization
        for (AbstractElectricalBlockEntity blockEntity : blockEntities) {
            // getConnectedSides() may compute on the fly, so we cache result here
            ArrayList<Boolean> connectedSides = blockEntity.getConnectedSides();

            for (int i = 0; i < blockEntity.getOutgoingNodes().size(); i++) {
                if (!connectedSides.get(i)) continue;

                int outgoingNode = blockEntity.getOutgoingNodes().get(i);

                if (!nodeReductionMap.containsKey(outgoingNode)) {
                    nodeReductionMap.put(outgoingNode, currentNodeID);
                    currentNodeID++;
                }

                int normalizedNodeId = nodeReductionMap.get(outgoingNode);
                blockEntity.getOutgoingNodes().set(i, normalizedNodeId);  // Replace preliminary node ID with permanent one for future reference
                blockEntity.setNormalizedOutgoingNode(normalizedNodeId, directions[i]); // Assign normalized node ID
            }

            blockEntity.computeConnectedSides();
            currentNodeID = addInternalCircuit(blockEntity.getInternalCircuit(), currentNodeID);
        }

        // Solve the circuit
        solve();

//        final long endTime = System.nanoTime();
//        double d = (endTime - startTime) * 1.0 / 1e6;
//        double d2 = (endTime - solveTime) * 1.0 / 1e6;
//        System.out.println("Total execution time: " + d + " ms, counts " + count + "  circuit size: " + circuit.getComponents().size());
//        System.out.println("Time solving:: " + d + " ms");
    }

    private int superconductorFloodfill(BlockPos start, Queue<BlockPos> posToVisit, int outgoingNode, int index) {
        Queue<BlockPos> superconductingPos = new LinkedList<>();
        superconductingPos.add(start);

        while (superconductingPos.size() > 0) {
            BlockPos p = superconductingPos.remove();
            BlockEntity entity = world.getBlockEntity(p);

            if (!(entity instanceof AbstractElectricalBlockEntity)) break;

            AbstractElectricalBlockEntity electricalEntity = ((AbstractElectricalBlockEntity)entity);
            if (!electricalEntity.isSuperconducting())
                continue;

            electricalEntity.setCircuit(this);

            for (Direction dir : Direction.values()) {
                BlockEntity newEntity = world.getBlockEntity(p.offset(dir));
                if (!(newEntity instanceof AbstractElectricalBlockEntity))
                    continue;

                AbstractElectricalBlockEntity eEntity = ((AbstractElectricalBlockEntity) newEntity);
                if (!(electricalEntity.getConnectedSides().get(indexFromDirection(dir)))) // No valid connection, checked in the entity
                    continue;

                if (eEntity.isSuperconducting() && eEntity.getCircuit() != this) {
                    if (eEntity.getCircuit() != null && !eEntity.getCircuit().id.equals(id))
                        ((IHasCircuitManager)world).getCircuitManager().deleteCircuit(eEntity.getCircuit());
                    superconductingPos.add(p.offset(dir));
                    continue;
                }

                // We already traversed this entity, add new connection
                // if (eEntity.getOutgoingNodes().size() > 0 && electricalEntity.getOutgoingNodes().size() > 0 && eEntity.getCircuit() == this)
                    // electricalEntity.setOutgoingNode(dir, outgoingNode);

                // New entity
                if (eEntity.getCircuit() != this) {
                    if (eEntity.getCircuit() != null && !eEntity.getCircuit().id.equals(id))
                        ((IHasCircuitManager)world).getCircuitManager().deleteCircuit(eEntity.getCircuit());

                    index++;
                    posToVisit.add(p.offset(dir));
                    eEntity.generatePreliminaryOutgoingNodes(index);
                    eEntity.setOutgoingNode(dir.getOpposite(), outgoingNode);
                }
            }
        }
        return index;
    }

    /**
     * Solves the internal circuit and assigns solved nodal voltages
     * to every block entity in the circuit
     */
    public void solve() {
        try {
            circuit.solve();

            for (AbstractElectricalBlockEntity ent : blockEntities) {
                ent.clearVoltages();
                for (int nodeId : ent.getNormalizedOutgoingNodes())
                    ent.setVoltage(nodeId, circuit.getNodalVoltage(nodeId));
            }
        }
        catch (Exception e) {
            System.out.println("failed to solve");
            System.out.println(e.toString());
        }
    }

    /**
     * Adds an internal circuit to the main circuit. The internal circuit should have
     * all outer nodes reassigned (nodes that can connect to other internal circuits)
     * to a normalized ID, while internal nodes should be a unique negative number.
     *
     * Internal nodes will be automatically reassigned by this.
     *
     * @param internalCircuit Internal circuit to merge
     * @param currentNodeID Current max node ID
     * @return new current node ID
     */
    private int addInternalCircuit(VirtualCircuit internalCircuit, int currentNodeID) {
        ArrayList<AbstractVirtualComponent> components = internalCircuit.getComponents();
        HashMap<Integer, Integer> seenNodes = new HashMap<>();

        for (AbstractVirtualComponent comp : components) {
            int node1 = comp.getNode1();
            int node2 = comp.getNode2();

            // Assign internal nodes if not assigned
            if (node1 < 0) {
                if (!seenNodes.containsKey(node1)) {
                    seenNodes.put(node1, currentNodeID);
                    node1 = currentNodeID;
                    currentNodeID++;
                }
                else { node1 = seenNodes.get(node1); }
            }
            if (node2 < 0) {
                if (!seenNodes.containsKey(node2)) {
                    seenNodes.put(node2, currentNodeID);
                    node2 = currentNodeID;
                    currentNodeID++;
                }
                else { node2 = seenNodes.get(node2); }
            }

            circuit.addComponent(comp, node1, node2);
        }
        return currentNodeID;
    }

    /**
     * Mark the circuit as dirty. It will be ticked and re-solved
     * next time CircuitManager reaches it.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Marks circuit as invalid, it will be re-constructed via
     * floodfill mechanism.
     */
    public void markInvalid() {
        invalid = true;
    }

    /**
     * Flag an element has been added to the circuit
     * @param pos An EXISTING circuit element that touches the element
     *            that is added. Note this is NOT the location of the
     *            element that was added!
     */
    public void flagElementAdded(BlockPos pos) {
        floodfillPos = pos;
        markInvalid();
    }

    /**
     * Flag an element has been removed.
     * @param pos Position of removed element
     */
    public void flagElementRemoved(BlockPos pos) {
        componentBeenRemoved = true;
        markInvalid();
    }

    /**
     * Flag circuit is scheduled to be deleted
     * Do not call directly, use circuit manager's deletion
     * scheduler instead.
     */
    public void flagForDeletion() { this.deleted = true; }

    /** Is circuit scheduled to be deleted? */
    public boolean isDeleted() { return this.deleted; }

    /**
     * Get the internal virtual circuit
     * @return Virtual circuit
     */
    public VirtualCircuit virtualCircuit() {
        return circuit;
    }

    /**
     * Returns an unique index from 0 to 5 inclusive given
     * a direction.
     * @param dir Direction
     * @return Index
     */
    public static int indexFromDirection(Direction dir) {
        return dir.ordinal();
    }
}
