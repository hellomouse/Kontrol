package net.hellomouse.kontrol.electrical.circuit;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.*;


/**
 * A physical Minecraft circuit, internally solved with a VirtualCircuit.
 * Handles blocks being deleted and added to a network.
 * @author Bowserinator
 */
public class Circuit {
    // Required:
    // map of node connections
    // map of node => pos and pos => node
    //

    private boolean dirty = true;   // Requires update?
    private boolean invalid = true; // Requires re-construction?

    // World
    private final ArrayList<AbstractElectricalBlockEntity> blockEntities = new ArrayList<>();
    private final World world;

    private long lastQueuedTick;

    private long lastUpdatedTick = -1;

    private final VirtualCircuit circuit = new VirtualCircuit();

    private BlockPos floodfillPos;
    private boolean removed = false;


    public Circuit(World world, BlockPos pos) {
        this.world = world;
        this.lastQueuedTick = -1;
        this.floodfillPos = pos;

        // Individual branches can be marked dirty as well
        // to avoid full recomputation?

        // Traverse all connected blocks, doing a floodfill to generate
        // enforce max length of wires maybe
        // or just max nodes to search per tick
        // Map blockPos => circuitNode and generate
        // AbstractElectricalBlock => return access to a Node object to set ID and get position??
        //    - nodeid, relative in block location, polarity
        // todo: figure out how to run after all blocks updated
        // or "queue" updates instead

        // TODO: figure out merging of circuits??
        // TODO: copy all things from TPT, like floating branches, branches, etc...
    }

    public void flagElementAdded(BlockPos pos) {
        floodfillPos = pos;
        markInvalid();
    }

    public void flagElementRemoved(BlockPos pos) {
        removed = true;
        markInvalid();
    }

    public void verifyIntegrity() {
        // Updates all values so others can get it



        // Circuit structure has been invalidated, re-do floodFill and
        // virtual circuit construction
        if (invalid) {
            if (removed) {
                System.out.println("A circuit elemnt was removed");
                floodfillPos = null;
                for (AbstractElectricalBlockEntity blockEntity : blockEntities) {
                    blockEntity.setCircuit(null);
                    if (floodfillPos == null && !blockEntity.isRemoved())
                        floodfillPos = blockEntity.getPos();
                }
            }

            if (floodfillPos != null) {
                System.out.println("Floodfill at " + floodfillPos);
                floodFill(floodfillPos);
                floodfillPos = null;
            }

            // System.out.println(world.getTime());

            lastUpdatedTick = world.getTimeOfDay();
            invalid = false;
            dirty = false;
            removed = false;
        }

        else if (dirty) {
            circuit.tick(); // TODO: elsewhere
            solve();
            lastUpdatedTick = world.getTimeOfDay();
            dirty = false;
        }
    }



    public void floodFill(BlockPos pos) {
        final long startTime = System.nanoTime();

        circuit.clear();

        // Reconsturct from blockpos
        System.out.println("\nFLOODFILL from " + pos);

        Queue<BlockPos> posToVisit = new LinkedList<>();
        posToVisit.add(pos);

        int count = 0;

        System.out.println("Starting: " + blockEntities.size());

        // TODO: if another circuit type was found delete self from existance



        for (AbstractElectricalBlockEntity e : blockEntities) {
            e.clearConnectedSides();
            e.setCircuit(null);
        }

        // TODO: clear
        blockEntities.clear();
        int index = 0;


        while (posToVisit.size() > 0) {
            BlockPos p = posToVisit.remove();
            BlockEntity entity = world.getBlockEntity(p);

            if (!(entity instanceof AbstractElectricalBlockEntity))
                break;

            AbstractElectricalBlockEntity electricalEntity = ((AbstractElectricalBlockEntity) entity);

            electricalEntity.setCircuit(this);
            //System.out.println(index + " " + index * 6 + " at " + electricalEntity.getPos());
            //System.out.println("Entities: " + entities.size());

            electricalEntity.generatePreliminaryOutgoingNodes(index);
            blockEntities.add(electricalEntity);

            count++;

            for (Direction dir : Direction.values()) {
                //TODO skip illegal connnections uysing entity computeConnectedSides

                BlockEntity newEntity = world.getBlockEntity(p.offset(dir));
                if (newEntity instanceof AbstractElectricalBlockEntity) {

                    AbstractElectricalBlockEntity eEntity = ((AbstractElectricalBlockEntity) newEntity);

                    if (!(electricalEntity.getConnectedSides().get(indexFromDirection(dir))))
                        continue;

                    if (eEntity.getOutgoingNodes().size() > 0 && electricalEntity.getOutgoingNodes().size() > 0 && eEntity.getCircuit() == this) {
                        //System.out.println(eEntity.getOutgoingNodes());
                        //System.out.println(electricalEntity.getOutgoingNodes());

                        electricalEntity.setOutgoingNode(dir, eEntity.getOutgoingNodes().get(indexFromDirection(dir.getOpposite())));
                        //System.out.println(electricalEntity.getPos() + " & " + eEntity.getPos() + " direction " + dir + " to " + eEntity.getOutgoingNodes().get(indexFromDirection(dir.getOpposite())));

                    }

                    if (eEntity.getCircuit() != this) {
                        posToVisit.add(p.offset(dir));
                        eEntity.setCircuit(this);
                    }
                }
            }

            index++;
        }


        // Solve the icrcuit
        // do node normalization
        Map<Integer, Integer> nodeReductionMap = new HashMap<>();
        int currentNodeID = 0;

        Direction[] directions = Direction.values();

        System.out.println("Size: " + blockEntities.size());

        for (AbstractElectricalBlockEntity blockEntity : blockEntities) {
            // System.out.println(blockEntity.getClass().getSimpleName() + "  " + blockEntity.getPos() + " | " + blockEntity.getOutgoingNodes());

            // getConnectedSides() may compute on the fly, so we cache result here
            ArrayList<Boolean> connectedSides = blockEntity.getConnectedSides();

            for (int i = 0; i < blockEntity.getOutgoingNodes().size(); i++) {
                if (!connectedSides.get(i))
                    continue;

                int outgoingNode = blockEntity.getOutgoingNodes().get(i);

                if (!nodeReductionMap.containsKey(outgoingNode)) {
                    nodeReductionMap.put(outgoingNode, currentNodeID);
                    // System.out.println("Assigning " + outgoingNode + " to " + currentNodeID);
                    currentNodeID++;
                }

                int normalizedNodeId = nodeReductionMap.get(outgoingNode);
                blockEntity.getOutgoingNodes().set(i, normalizedNodeId);
                blockEntity.setNormalizedOutgoingNode(normalizedNodeId, directions[i]);
            }

            blockEntity.computeConnectedSides();

            // System.out.println(blockEntity.getOutgoingNodes() + " | " + blockEntity.getConnectedSides());

            currentNodeID = addInternalCircuit(blockEntity.getInternalCircuit(), blockEntity.getNormalizedOutgoingNodes(), currentNodeID);
        }

        // System.out.println(nodeReductionMap);
        //for (AbstractVirtualComponent c : circuit.getComponents())
        //    System.out.println(c.getCondition());


        final long solveTime = System.nanoTime();
        solve();

        final long endTime = System.nanoTime();
        double d = (endTime - startTime) * 1.0 / 1e6;
        double d2 = (endTime - solveTime) * 1.0 / 1e6;
        System.out.println("Total execution time: " + d + " ms, counts " + count + "  circuit size: " + circuit.getComponents().size());
        System.out.println("Time solving:: " + d + " ms");



//        long worldTime = world.getLunarTime();
//        if (worldTime != lastQueuedTick) {
//            lastQueuedTick = worldTime;
//
//        }
    }

    public void solve() {
        try {
            circuit.solve();

            for (AbstractElectricalBlockEntity ent : blockEntities) {
                ent.clearVoltages();
                for (int nodeId : ent.getNormalizedOutgoingNodes())
                    ent.setVoltage(nodeId, circuit.getNodalVoltage(nodeId));
            }

            //if (circuit.getComponents().size() > 0)
            //    for (int i = 0; i <= circuit.getHighestNodeID(); i++)
            //        System.out.println(circuit.getNodalVoltage(i));
        }
        catch (Exception e) {
            System.out.println("failed to solve");
            System.out.println(e.toString());
        }
    }


    // --- Circuit component modification --- \\
    private int addInternalCircuit(VirtualCircuit internalCircuit, ArrayList<Integer> outgoingNodes, int currentNodeID) {
        // TODO: merge outgoing nodes somehow??
        // maybe assign outgoing nodes that are the same first?

        ArrayList<AbstractVirtualComponent> components = internalCircuit.getComponents();
        HashMap<Integer, Integer> seenNodes = new HashMap<>();

       // System.out.println("Hmm " + outgoingNodes);

        for (AbstractVirtualComponent comp : components) {
            int node1 = comp.getNode1();
            int node2 = comp.getNode2();

            if (node1 < 0) {
                if (!seenNodes.containsKey(node1)) {
                    seenNodes.put(node1, currentNodeID);
                    //System.out.println("Assigned internal node " + node1 + " to " + currentNodeID);
                    node1 = currentNodeID;
                    currentNodeID++;
                }
                else
                    node1 = seenNodes.get(node1);
            }
            if (node2 < 0) {
                if (!seenNodes.containsKey(node2)) {
                    seenNodes.put(node2, currentNodeID);
                    //System.out.println("Assigned internal node " + node2 + " to " + currentNodeID);
                    node2 = currentNodeID;
                    currentNodeID++;
                }
                else
                    node2 = seenNodes.get(node2);
            }

            //System.out.println("  > Adding component  " +comp.getClass().getSimpleName() + " from " + node1 + ", " + node2);
            circuit.addComponent(comp, node1, node2);
        }

        // System.out.println("---\n");
        return currentNodeID;
    }


    // .tick() - called from circuitManager
    // .getCurrent and voltages at virtual nodes
    //   TODO: offset map for nodeIDs allow translation for internalCircuit and external

    public void markDirty() {  // Values changed, but not thing?
        dirty = true;
        lastQueuedTick = world.getLunarTime();
    }

    public void markInvalid() {
        invalid = true;
        lastQueuedTick = world.getLunarTime();
    }

    public VirtualCircuit virtualCircuit() {
        return circuit;
    }


    public static int indexFromDirection(Direction dir) {
        return dir.ordinal();
    }
}
