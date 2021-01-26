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
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.ONE_TAU;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * RL circuit tests. See PolarityTests for polarities.
 * @author Bowserinator
 */
class RLCircuitTests {
    /**
     * Initially inductor is open circuit, check if no current across
     * resistors and all voltage drop is across the inductor.
     */
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

        // Initially inductor is open circuit
        assertEquals(-10.0, L1.getVoltage(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(0.0, R2.getVoltage(), EPSILON);
    }

    /**
     * At steady state inductor is short, verify no voltage across inductor and
     * proper currents through resistors.
     *
     * RL time constant is ~0.01s, so we simulate for 1 second to reach steady state.
     */
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

        for (int i = 0; i < 1 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally inductor is short
        assertEquals(0.0, L1.getVoltage(), EPSILON);
        assertEquals(-10.0 * 1000 / 1001, R1.getVoltage(), EPSILON);
        assertEquals(-10.0 / 1001, R2.getVoltage(), EPSILON);
    }

    /**
     * Uses a 1 ohm and 0.001 H inductor so RL time constant is 0.001s, much
     * smaller than a single integration time step. The circuit should skip to
     * steady state values instead of blowing up to infinite voltages.
     */
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

        for (int i = 0; i < 1 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }

        // Finally inductor is short
        assertEquals(0.0, L1.getVoltage(), EPSILON);
        assertEquals(-5.0, R1.getVoltage(), EPSILON);
        assertEquals(-5.0, R2.getVoltage(), EPSILON);
    }

    /**
     * Time constant is 100 s, so we simulate the circuit for 100 s (1 time
     * constant) and verify current reaches 63.2% of steady state value.
     *
     * There is extra allowance in the epsilon to allow for minor integration error.
     */
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
        assertEquals(-2 * ONE_TAU, L1.getVoltage(), 0.1);
    }

    /**
     * Same as test4, but using 2 150 H inductors for equivalent inductance
     * of 300 H for 1 time constant of 100 s.
     *
     * @see EdgeCaseTests#test9()
     */
    @Test
    @DisplayName("1 ohm in series 10 V, 2 1 ohm and 2 150 H inductor - 1 time constant")
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

        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(new VirtualResistor(1e3), 2, 3);
        circuit.addComponent(R2, 3, 4);
        circuit.addComponent(L2, 4, 5);
        circuit.addComponent(new VirtualResistor(1e3), 4, 5);
        circuit.addComponent(R3, 5, 0);

        circuit.addComponent(new VirtualGround(), 0, 0);

        circuit.solve();

        // RL time constant = 300 / 3 = 100s, divide by DT to tick that many times
        int iterations = (int)(100.0 / DT);
        for (int i = 0; i < iterations; i++) { // iterations
            circuit.tick();
            circuit.solve();
        }

        // (Combined) Inductor should reach ~63.2% of final current of 10 / 3 A
        assertEquals(10.0 / 3.0 * ONE_TAU, R1.getCurrent(), 0.2);
    }

    /**
     * Same as test4, but using 2 200 H inductors for equivalent inductance
     * of 100 H for 1 time constant, should be same result as test4
     */
    @Test
    @DisplayName("1 ohm in series 10 V, 1 ohm and (2 200 H inductor in parallel) - 1 time constant")
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
        assertEquals(ONE_TAU * 5.0, R1.getCurrent(), 0.2);
    }
}
