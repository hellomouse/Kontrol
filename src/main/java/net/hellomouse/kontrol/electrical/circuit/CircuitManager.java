package net.hellomouse.kontrol.electrical.circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;


/**
 * A per-world circuit manager. Ticks all circuits, adds and removes them.
 * @see net.hellomouse.kontrol.electrical.mixin.CircuitTickMixin
 * @author Bowserinator
 */
public class CircuitManager {
    private final Map<UUID, Circuit> circuitMap = new HashMap<>();
    private final HashSet<UUID> idsToDelete = new HashSet<>();

    /**
     * Run after ticking block entities. Deletes circuits scheduled for
     * deletion and postTicks() all circuits
     */
    public void postTick() {
        // TODO: profiler

        for (UUID id : idsToDelete)
            circuitMap.remove(id);
        idsToDelete.clear();

        for (Circuit circuit : circuitMap.values())
            circuit.postTick();
    }

    /**
     * Adds a new circuit and returns it if adding was
     * successful, otherwise returns null (circuit id already exists)
     * @param circuit Circuit to add
     * @return Circuit if added, otherwise null
     */
    public Circuit addCircuit(Circuit circuit) {
        if (circuitMap.containsKey(circuit.id))
            return null;
        circuitMap.put(circuit.id, circuit);
        return circuit;
    }

    /**
     * Schedule a circuit for deletion
     * @param circuit Circuit to delete
     */
    public void deleteCircuit(Circuit circuit) {
        idsToDelete.add(circuit.id);
        circuit.flagForDeletion();
    }
}
