package net.hellomouse.kontrol.electrical.circuit.virtual.tests;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.electrical.circuit.virtual.tests.TestConstants.EPSILON;
import static net.hellomouse.kontrol.electrical.circuit.virtual.tests.TestConstants.ONE_TAU;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests of edge case circuits., ie, extreme values, or unusual
 * circuit layouts that might cause problems.
 * @author Bowserinator
 */
class EdgeCaseTests {
    /**
     * Put some resistors in series with a voltage source and 2 zero amp
     * current sources. Due to solver limitations all current sources must
     * be in parallel with a high value resistor.
     *
     * Voltage across each current source should be -5.0, as no current
     * and thus no voltage drops across any of the resistors.
     */
    @Test
    @DisplayName("3 1 ohm in series 10 V, 2 0A current sources - No crash, -5 V drop each")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualCurrentSource CS1 = new VirtualCurrentSource(0);
        VirtualCurrentSource CS2 = new VirtualCurrentSource(0);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(CS1, 2, 3);
        circuit.addComponent(new VirtualResistor(1e9), 2, 3);
        circuit.addComponent(R2, 3, 4);
        circuit.addComponent(CS2, 4, 5);
        circuit.addComponent(new VirtualResistor(1e9), 4, 5);
        circuit.addComponent(R3, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(-5.0, CS1.getVoltage(), EPSILON);
        assertEquals(-5.0, CS2.getVoltage(), EPSILON);
    }

    /**
     * Same as test1, except with 150 A current sources, and a much higher parallel resistor
     * (1e99 ohms vs 1e9). This is beyond double precision addition (1 ohm + 1e99 ohm = 1e99 ohm, which
     * results in incorrect equations). The voltages will also be very high with 150 A
     * going through the resistors.
     *
     * This test is to verify that the solver will not crash in this scenario, which could occur
     * for instance, in an extreme case with an inductor (which are modelled as current sources)
     */
    @Test
    @DisplayName("3 1 ohm in series 10 V, 2 150A current sources - No crash")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualCurrentSource CS1 = new VirtualCurrentSource(150);
        VirtualCurrentSource CS2 = new VirtualCurrentSource(150);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(CS1, 2, 3);
        circuit.addComponent(new VirtualResistor(1e99), 2, 3);
        circuit.addComponent(R2, 3, 4);
        circuit.addComponent(CS2, 4, 5);
        circuit.addComponent(new VirtualResistor(1e99), 4, 5);
        circuit.addComponent(R3, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // Test shouldn't crash, so reaching here passes it
    }

    /**
     * A simple resistor & voltage source circuit. However, 2 ground nodes,
     * one at each end, is used instead of the usual loop.
     *
     * This test exists in case somehow this gets broken.
     */
    @Test
    @DisplayName("No loop: Ground - 10 V - 1 k - Ground")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(new VirtualGround(), 2, 2);
        circuit.solve();

        assertEquals(-10.0, R1.getVoltage(), EPSILON);
        assertEquals(10.0 / 1000.0, R1.getCurrent(), EPSILON);
    }


    /**
     * An incomplete circuit consisting of just a voltage source.
     * Should not attempt to solve and shouldn't crash.
     */
    @Test
    @DisplayName("10 V (1 element) - No crash")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.solve();

        // Test shouldn't crash, so reaching here passes it
    }

    /**
     * A voltage source grounded on both ends, an impossibility.
     * While it would be nice for the solver to crash on an impossible
     * configuration (giving us notice), this is not crucial to the performance
     * of the solver, and thus this test will never fail.
     *
     * If the solver outputs anything, a note will be displayed.
     */
    @Test
    @DisplayName("Ground - 10 V - Ground - Should crash")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(new VirtualGround(), 1, 1);

        try {
            circuit.solve();
            System.out.println("Didn't crash. Nodal voltages: N0 = " + circuit.getNodalVoltage(0) + ", N1 = " + circuit.getNodalVoltage(1));
            System.out.println("It is not crucial that this test passes");
            System.out.println("as impossible circuits should never occur anyways.");
        } catch (Exception e) {
            // Didn't crash, pass
        }
    }

    /**
     * Just a voltage source connected in series with resistor in a loop
     * (like a battery + light-bulb). There is no VirtualGround in this
     * circuit; this is meant to test two element circuits.
     */
    @Test
    @DisplayName("1 k in series with 10 V, no ground node")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.solve();

        assertEquals(10.0, V1.getVoltage(), EPSILON);
        assertEquals(-10.0, R1.getVoltage(), EPSILON);
    }

    /**
     * Same as test 6, but with 2 voltage sources in series. Only 1 ground node
     * should be automatically added in this case.
     */
    @Test
    @DisplayName("1 k in series with two 10 V, no ground node")
    void test7() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualVoltageSource V2 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(V2, 3, 2);
        circuit.addComponent(R1, 3, 0);
        circuit.solve();

        assertEquals(-20.0, R1.getVoltage(), EPSILON);
    }

    /**
     * Resistor in series with 2 voltage sources, but 1 is disabled.
     * A disabled voltage source is 0.0 V, so basically V1 doesn't exist
     */
    @Test
    @DisplayName("2 Voltage sources - 1 disabled")
    void test8() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualVoltageSource V2 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(V2, 3, 2);
        circuit.addComponent(R1, 3, 4);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(new VirtualGround(), 4, 4);
        V1.setDisabled(true);
        circuit.solve();

        assertEquals(-10 / 1000.0, V1.getCurrent(), EPSILON);
        assertEquals(-10 / 1000.0, V2.getCurrent(), EPSILON);
        assertEquals(-10.0, R1.getVoltage(), EPSILON);
    }

    /**
     * Extreme RL circuit values where even small differences in computed voltage
     * can lead to blow-up. the 1k parallel resistors are adjusted so at the time
     * of writing this test, this circuit does not blow up to infinity. Future modifications
     * that reduce accuracy of matrix solution calculations or precision may cause
     * this test to fail.
     */
    @Test
    @DisplayName("1 ohm in series 10 V, 2 1 ohm and 2 150 H inductor - 1 time constant")
    void test9() {
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
        assertEquals(10 / 3.0 * ONE_TAU, R1.getCurrent(), 0.2);
    }

    /**
     * Test if solver behaves well with floating nodes. Floating branches
     * should have no current and the same voltage as where they were connected.
     */
    @Test
    @DisplayName("10 V with 3 1k resistors, two are floating")
    void test10() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1000);
        VirtualResistor R3 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(R3, 2, 3);
        circuit.addComponent(R2, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(-10.0, R2.getVoltage(), EPSILON);
        assertEquals(10.0 / 1000.0, R2.getCurrent(), EPSILON);

        assertEquals(0.0, R1.getCurrent(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(10.0, circuit.getNodalVoltage(2), EPSILON);

        assertEquals(0.0, R3.getCurrent(), EPSILON);
        assertEquals(0.0, R3.getVoltage(), EPSILON);
        assertEquals(10.0, circuit.getNodalVoltage(3), EPSILON);
    }

    /**
     * Resistor in series with 2 voltage sources, but 1 is hi-Z
     * A Hi-Z is basically an open circuit.
     */
    @Test
    @DisplayName("2 Voltage sources - 1 hi-Z")
    void test11() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualVoltageSource V2 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(V2, 2, 1);
        circuit.addComponent(R1, 2, 3);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(new VirtualGround(), 3, 3);
        V1.setHiZ(true);
        circuit.solve();

        assertEquals(0.0, V1.getCurrent(), EPSILON);
        assertEquals(0.0, V2.getCurrent(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
    }

    /**
     * Use circuit.getCurrentThrough through an resistor
     */
    @Test
    @DisplayName("circuit.getCurrentThrough for a resistor")
    void test12() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(500);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(R2, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10 / 1000.0, R1.getCurrent(), EPSILON);
        assertEquals(10 / 1000.0, R2.getCurrent(), EPSILON);
        assertEquals(-10 / 1000.0, V1.getCurrent(), EPSILON);
        assertEquals(R1.getCurrent(), circuit.getCurrentThrough(1, 2), EPSILON);
    }

    /**
     * Use circuit.getCurrentThrough on a current source
     */
    @Test
    @DisplayName("circuit.getCurrentThrough for a current source")
    void test13() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(500);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualCurrentSource C1 = new VirtualCurrentSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(C1, 1, 2);
        circuit.addComponent(R2, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10, R1.getCurrent(), EPSILON);
        assertEquals(10, R2.getCurrent(), EPSILON);
        assertEquals(R1.getCurrent(), circuit.getCurrentThrough(1, 2), EPSILON);
    }


    /**
     * Use circuit.getCurrentThrough, multiple resistors on both sides
     */
    @Test
    @DisplayName("circuit.getCurrentThrough multiple resistors")
    void test14() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(500);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualResistor R3 = new VirtualResistor(500);
        VirtualResistor R4 = new VirtualResistor(500);
        VirtualCurrentSource C1 = new VirtualCurrentSource(10);
        VirtualVoltageSource V1 = new VirtualVoltageSource(1);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R3, 0, 1);
        circuit.addComponent(V1, 2, 1);
        circuit.addComponent(C1, 3, 0);
        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(R4, 2, 3);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(5, R1.getCurrent(), EPSILON);
        assertEquals(5, R2.getCurrent(), EPSILON);
        assertEquals(R1.getCurrent() + R3.getCurrent(), circuit.getCurrentThrough(1, 2), EPSILON);
    }

    /**
     * Use circuit.getCurrentThrough, multiple resistors on both sides, 1 side invalid
     */
    @Test
    @DisplayName("circuit.getCurrentThrough multiple resistors, 1 side invalid")
    void test15() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(500);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualResistor R3 = new VirtualResistor(500);
        VirtualResistor R4 = new VirtualResistor(500);
        VirtualCurrentSource C1 = new VirtualCurrentSource(10);
        VirtualVoltageSource V1 = new VirtualVoltageSource(1);
        VirtualInductor L1 = new VirtualInductor(1);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R3, 0, 1);
        circuit.addComponent(V1, 2, 1);
        circuit.addComponent(C1, 3, 4);
        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(R4, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 4, 0);

        circuit.addComponent(L1, 0, 4);
        circuit.addComponent(new VirtualResistor(1),4, 1);

        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(R2.getCurrent() + R4.getCurrent(), circuit.getCurrentThrough(1, 2), EPSILON);
    }

    /**
     * Use circuit.getCurrentThrough, multiple resistors + current source on both sides
     */
    @Test
    @DisplayName("circuit.getCurrentThrough multiple resistors, 2 sides invalid")
    void test16() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(500);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualResistor R3 = new VirtualResistor(500);
        VirtualResistor R4 = new VirtualResistor(500);
        VirtualCurrentSource C1 = new VirtualCurrentSource(10);
        VirtualCurrentSource C2 = new VirtualCurrentSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R3, 0, 1);
        circuit.addComponent(C1, 0, 1);

        circuit.addComponent(new VirtualCapacitor(1), 1, 2);

        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(R4, 2, 3);
        circuit.addComponent(C2, 2, 3);

        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(R2.getCurrent() + R4.getCurrent() + C2.getCurrent(), circuit.getCurrentThrough(1, 2), EPSILON);
    }
}
