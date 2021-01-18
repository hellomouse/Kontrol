package net.hellomouse.kontrol.logic.circuit;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * Applies changes from the Minecraft world to the virtual circuit
 * and vice versa. Only updates when markDirty() is called, and updates
 * on the next tick after being marked.
 */
public class Circuit {
    // Required:
    // map of node connections
    // map of node => pos and pos => node
    //

    private boolean dirty; // Requires update?
    private WorldAccess world;
    private long lastQueuedTick;
    private VirtualCircuit circuit;

    public Circuit(BlockPos startPos, WorldAccess world) {
        this.dirty = true;
        this.world = world;
        this.lastQueuedTick = -1;

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

    public void update() {
        if (!dirty) return; // No changes have occurred
        
        long worldTime = world.getLunarTime();
        if (worldTime != lastQueuedTick) {
            lastQueuedTick = worldTime;

        }
    }


    public void markDirty() {
        dirty = true;
        lastQueuedTick = world.getLunarTime();
    }

    public void addNode(BlockPos pos) {
        // Check node connections
        // each block tracks start and end node?
        //
        markDirty();
    }

    public void removeNode() {
        markDirty();
    }

    public void getVoltage() {

    }

    public void getCurrent() {

    }


    // --- Circuit generation --- \\
    private void generateCircuit(BlockPos startPos) {

    }

    private void generateBranch(BlockPos startPos, Direction dir) {

    }
}
