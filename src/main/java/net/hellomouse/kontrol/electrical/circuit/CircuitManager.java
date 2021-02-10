package net.hellomouse.kontrol.electrical.circuit;

import java.util.ArrayList;

public class CircuitManager {
    private ArrayList<Circuit> circuits = new ArrayList<>();

    public void postTick() {
        // TODO: profiler
        for (Circuit c : circuits)
            c.verifyIntegrity();
    }

    public void addCircuit(Circuit c) {
        circuits.add(c);
    }
}
