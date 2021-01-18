package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualInductor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualGround;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualVoltageSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

// See ResistorTests for resistor and voltage polarities

class RLCircuitTest {
    @Test
    @DisplayName("1k ohm in series 10 V, 1 ohm and 10 H inductor - initial state")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        // || -- V1 -- R1 -- L1 -- R2 -- ||

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(10);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // Initially inductor is closed
        assertEquals(-10.0, L1.getVoltage(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(0.0, R2.getVoltage(), EPSILON);
    }

    @Test
    @DisplayName("1k ohm in series 10 V, 1 ohm and 10 H inductor - steady state")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(10);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < 100; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally inductor is open
        assertEquals(0.0, L1.getVoltage(), EPSILON);
        assertEquals(-10.0 * 1000 / 1001, R1.getVoltage(), EPSILON);
        assertEquals(-10.0 / 1001, R2.getVoltage(), EPSILON);
    }

    @Test
    @DisplayName("1 ohm in series 10 V, 1 ohm and 0.001 H inductor - divergence test")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(1e-3);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < 100; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally inductor is open
        assertEquals(0.0, L1.getVoltage(), EPSILON);
        assertEquals(-5.0, R1.getVoltage(), EPSILON);
        assertEquals(-5.0, R2.getVoltage(), EPSILON);
    }

    @Test
    @DisplayName("1 ohm in series 10 V, 1 ohm and 100 H inductor - 1 time constant")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(100);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // RL time constant = 100 / 1 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Inductor should reach ~63.2% of final current of 2 A
        assertEquals(-2 * 0.63212055882, L1.getVoltage(), 0.1);
    }

    @Test
    @DisplayName("1 ohm in series 10 V, 2 1 ohm and 2 150 H capacitor - 1 time constant")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(150);
        VirtualInductor L2 = new VirtualInductor(150);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);

//        circuit.addComponent(new VirtualResistor(0.0001), 2, 3);
//        circuit.addComponent(L1, 3, 4);
//        circuit.addComponent(new VirtualResistor(0.0001), 4, 5);
//        circuit.addComponent(new VirtualResistor(1e12), 2, 5);
//
//        circuit.addComponent(R2, 5, 6);
//
//        circuit.addComponent(new VirtualResistor(0.0001), 6, 7);
//        circuit.addComponent(L2, 7, 8);
//        circuit.addComponent(new VirtualResistor(0.0001), 8, 9);
//        circuit.addComponent(new VirtualResistor(1e12), 6, 9);
//
//        circuit.addComponent(R3, 9, 0);

        // TODO dont hardcode 1e9
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(new VirtualResistor(1e2), 2, 3);
        circuit.addComponent(R2, 3, 4);
        circuit.addComponent(L2, 4, 5);
        circuit.addComponent(new VirtualResistor(1e2), 4, 5);
        circuit.addComponent(R3, 5, 0);

        circuit.addComponent(new VirtualGround(), 0, 0);

        circuit.solve();

        // RL time constant = 300 / 3 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) { // iterations
//            if (i < 5) {
//                System.out.println("");
//                System.out.println(L1.getVoltage() + " | " + L2.getVoltage() +", " + L1.getCurrent() + ", " + L2.getCurrent());
//                for (int j = 0; j < 5; j++)
//                    System.out.print(circuit.getNodalVoltage(j) + ", ");
//                System.out.println("\n---\n\n");
//            }
            circuit.tick();
            circuit.solve();
        }

        // Inductors dont share impedance in series :(

        System.out.println(L1.getCurrent());

        // (Combined) Inductor should reach ~63.2% of final current of 10 / 3 A
        assertEquals(-10 / 3.0 * 0.63212055882, R1.getCurrent(), 0.2);
    }

    @Test
    @DisplayName("1 ohm in series 10 V, 1 ohm and (2 200 H capacitor in parallel) - 1 time constant")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualResistor R4 = new VirtualResistor(1);
        VirtualResistor R5 = new VirtualResistor(1);
        VirtualResistor R6 = new VirtualResistor(1);

        VirtualInductor L1 = new VirtualInductor(200);
        VirtualInductor L2 = new VirtualInductor(200);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);

        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(L1, 3, 4);
        circuit.addComponent(R3, 4, 5);

        circuit.addComponent(R4, 2, 6);
        circuit.addComponent(L2, 6, 7);
        circuit.addComponent(R5, 7, 5);

        circuit.addComponent(R6, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // RL time constant = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) {
            circuit.tick();
            circuit.solve();
        }

        // R1 should reach ~63.2% of final current
        assertEquals(-0.63212055882 * 5.0, R1.getCurrent(), 0.2);
    }
}
