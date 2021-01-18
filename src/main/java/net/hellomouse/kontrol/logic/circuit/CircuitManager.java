package net.hellomouse.kontrol.logic.circuit;

import net.hellomouse.kontrol.blocks.electrical.AbstractElectricalBlock;
import net.hellomouse.kontrol.logic.circuit.Circuit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;

public class CircuitManager {
    private static final ArrayList<Circuit> circuitSolvers = new ArrayList<>();
    private static final ArrayList<Integer> freeIds = new ArrayList<>();

    public void addBlock(AbstractElectricalBlock block, BlockPos pos, WorldAccess world) {
        if (block.getGroupID() < 0) {
            if (freeIds.size() > 0) {   // Reuse ID from freeId list
                block.setGroupID(freeIds.get(0));
                freeIds.remove(0);
                circuitSolvers.set(block.getGroupID(), new Circuit(pos, world));
            }
            else {
                block.setGroupID(circuitSolvers.size());
                circuitSolvers.add(new Circuit(pos, world));
            }
        }
    }

    public void removeID(int index) {
        if (index >= 0 && index < circuitSolvers.size() && circuitSolvers.get(index) != null) {
            circuitSolvers.set(index, null);
            freeIds.add(index);
        }
    }
}
