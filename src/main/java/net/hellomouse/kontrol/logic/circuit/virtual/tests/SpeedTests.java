package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Just for measuring performance, always pass unless
 * something crashes. Recommended to run one by one
 * as there might be optimization behind the scenes.
 * @author Bowserinator
 */
class SpeedTests {
    /**
     * Adds 1 million resistors from node 0 to 1. Tests how long
     * it takes to process adding components. At time of writing is
     * a bit over 0.1 us per component.
     */
    @Test
    @DisplayName("Adding 1,000,000 components")
    void test1() {
        System.gc();
        System.runFinalization();

        long startTime = System.nanoTime();
        int total = 1000000;

        VirtualCircuit circuit = new VirtualCircuit();
        for (int i = 0; i < total; i++)
            circuit.addComponent(new VirtualResistor(1), 0, 1);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to add 1 million components:");
        System.out.println((duration / 1e6) + " ms (" + (duration / total / 1000.0) + " us per component)");
    }

    /**
     * Simple circuit with ground node defined. Base solving time
     * for 2 component circuit.
     */
    @Test
    @DisplayName("Voltage source + resistor solve time (with ground)")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualResistor R1 = new VirtualResistor(1000);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(new VirtualGround(), 0, 0);

        long startTime = System.nanoTime();
        circuit.solve();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 2 component circuit (with ground):");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Simple circuit with ground node defined. Helps figure out if
     * the ground node auto-adding code is slow.
     */
    @Test
    @DisplayName("Voltage source + resistor solve time (no ground)")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualResistor R1 = new VirtualResistor(1000);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 0, 1);

        long startTime = System.nanoTime();
        circuit.solve();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 2 component circuit (without ground):");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Voltage source + 1000 resistors in parallel. Only 2 nodes exist.
     * Test of how fast it takes to solve.
     */
    @Test
    @DisplayName("Solving 1000 resistors in parallel")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        for (int i = 0; i < 1000; i++)
            circuit.addComponent(new VirtualResistor(1000), 0, 1);
        circuit.addComponent(new VirtualGround(), 0, 0);

        long startTime = System.nanoTime();
        circuit.solve();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 1000 resistors in parallel:");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Voltage source + 1000 resistors in series. 1002 nodes exist.
     * Test of how fast it takes to solve.
     */
    @Test
    @DisplayName("Solving 1000 resistors in series")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        for (int i = 0; i < 1000; i++)
            circuit.addComponent(new VirtualResistor(1000), i + 1, i + 2);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(new VirtualGround(), 1002, 1002);

        long startTime = System.nanoTime();
        circuit.solve();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 1000 resistors in series:");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Test repeated circuit ticking and solving. This is not particularly
     * important as circuit ticks only once per in game tick, not all at once.
     */
    @Test
    @DisplayName("10,000 iterations of an RC circuit")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualCapacitor C1 = new VirtualCapacitor(0.1);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.01), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R1, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);

        long startTime = System.nanoTime();
        circuit.solve();

        for (int i = 0; i < 10000; i++) {
            circuit.tick();
            circuit.solve();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 10,000 iterations of RC Circuit");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Like test6, but diodes trigger a re-solve for the diode condition.
     * This diode is always on.
     */
    @Test
    @DisplayName("10,000 iterations of an RC circuit with diode - correct direction")
    void test7() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualCapacitor C1 = new VirtualCapacitor(0.1);
        VirtualDiode D1 = new VirtualDiode(0.8);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.01), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R1, 3, 4);
        circuit.addComponent(D1, 4, 5);
        circuit.addComponent(new VirtualResistor(0.01), 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);

        long startTime = System.nanoTime();
        circuit.solve();

        for (int i = 0; i < 10000; i++) {
            circuit.tick();
            circuit.solve();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 10,000 iterations of RC Circuit w/ forward diode");
        System.out.println((duration / 1e6) + " ms");
    }

    /**
     * Like test6, but diodes trigger a re-solve for the diode condition.
     * This diode is always off. This can test any diode optimizations implemented
     * (ie, guessing diode state)
     */
    @Test
    @DisplayName("10,000 iterations of an RC circuit with diode - wrong direction")
    void test8() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualCapacitor C1 = new VirtualCapacitor(0.1);
        VirtualDiode D1 = new VirtualDiode(0.8);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.01), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R1, 3, 4);
        circuit.addComponent(D1, 5, 4);
        circuit.addComponent(new VirtualResistor(0.01), 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);

        long startTime = System.nanoTime();
        circuit.solve();

        for (int i = 0; i < 10000; i++) {
            circuit.tick();
            circuit.solve();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to solve 10,000 iterations of RC Circuit w/ backward diode");
        System.out.println((duration / 1e6) + " ms");
    }
}
