package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualCapacitor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualGround;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualVoltageSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.ONE_TAU;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * RC Circuit tests.
 * See ResistorTests, PolarityTests for resistor and voltage polarities.
 * Capacitors drop voltage going from node1 to node2 when charging
 * @author Bowserinator
 */
class RCCircuitTests {
    /**
     * Testing capacitor initial state, where it behaves like
     * a short circuit.
     */
    @Test
    @DisplayName("1k ohm in series 10 V, 1 ohm and 100 uF capacitor - initial state")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        // || -- V1 -- R1 -- C1 -- R2 -- ||

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(100e-6);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // Initially capacitor is open
        assertEquals(0.0, C1.getVoltage(), EPSILON);
        assertEquals(-10.0 * (1000 / 1001.0), R1.getVoltage(), EPSILON);
        assertEquals(-10.0 * (1 / 1001.0), R2.getVoltage(), EPSILON);
    }

    /**
     * Testing capacitor steady state where it behaves like an open
     * circuit. We simulate for 1 second, which is 10x times the RC
     * time constant of 0.1s to reach steady state.
     */
    @Test
    @DisplayName("1k ohm in series 10 V, 1 ohm and 100 uF capacitor - steady state")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(100e-6);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < 1 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally capacitor is closed
        assertEquals(10.0, C1.getVoltage(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(0.0, R2.getVoltage(), EPSILON);
    }

    /**
     * This RC circuit has a very small time constant (1 ohm * 1e-6 F = 1e-6 s)
     * This is many times below the time step used in integration. This circuit
     * should not diverge and result in infinite voltages, but have expected steady state
     * values.
     */
    @Test
    @DisplayName("1 ohm in series 10 V, 1 ohm and 1 uF capacitor - divergence test")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(10);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(1e-6);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < 1 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally capacitor is closed
        assertEquals(10.0, C1.getVoltage(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(0.0, R2.getVoltage(), EPSILON);
    }

    /**
     * Simulate for 1 time constant, and check if capacitor voltage is ~63.2% of
     * final charge (as expected for 1 time constant). The epsilon allowance is increased
     * to allow for errors in numeric integration.
     */
    @Test
    @DisplayName("100 ohm in series 10 V, 1 ohm and 1 F capacitor - 1 time constant")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(100);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // RC time constant = 100 * 1 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Capacitor should reach ~63.2% of final charge of 10 V
        assertEquals(10 * ONE_TAU, C1.getVoltage(), 0.1);
    }

    /**
     * Using capacitors in series simulated for 1 time constant. Result should
     * be as expected with an equivalent 1 F capacitor.
     */
    @Test
    @DisplayName("100 ohm in series 10 V, 2 1 ohm and 2 2F capacitor - 1 time constant")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(100);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(2);
        VirtualCapacitor C2 = new VirtualCapacitor(2);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 4);
        circuit.addComponent(C2, 4, 5);
        circuit.addComponent(R3, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // RC time constant = 100 * 1 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) {
            circuit.tick();
            circuit.solve();
        }

        // (Combined) Capacitor should reach ~63.2% of final charge of 10 V
        assertEquals(10 * ONE_TAU, C2.getVoltage() + R2.getVoltage() + C1.getVoltage(), 0.2);
    }

    /**
     * Using capacitors in parallel simulated for 1 time constant. Result should
     * be as expected with an equivalent 1 F capacitor.
     */
    @Test
    @DisplayName("100 ohm in series 10 V, 1 ohm and (2 .5F capacitor in parallel) - 1 time constant")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(100);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualResistor R4 = new VirtualResistor(1);
        VirtualResistor R5 = new VirtualResistor(1);
        VirtualResistor R6 = new VirtualResistor(1);

        VirtualCapacitor C1 = new VirtualCapacitor(0.5);
        VirtualCapacitor C2 = new VirtualCapacitor(0.5);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);

        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(C1, 3, 4);
        circuit.addComponent(R3, 4, 5);

        circuit.addComponent(R4, 2, 6);
        circuit.addComponent(C2, 6, 7);
        circuit.addComponent(R5, 7, 5);

        circuit.addComponent(R6, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // RC time constant = 100 * 1 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Each capacitor should reach ~63.2% of final charge of 10 V
        assertEquals(10 * ONE_TAU, C1.getVoltage(), 0.2);
        assertEquals(10 * ONE_TAU, C2.getVoltage(), 0.2);
    }
}
