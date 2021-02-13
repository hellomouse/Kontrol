package net.hellomouse.kontrol.electrical.circuit.virtual.tests;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualGround;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualVoltageSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.electrical.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * For voltage sources, node1 is the + terminal, node2 is -
 * for resistors, voltage is negative going from node1 to node2
 * and current is positive going from node1 to node2
 *
 * See PolarityTests for more info
 *
 * *Note: most of the voltage sources in the tests below are "backwards"
 *  relative to the polarity of the resistor, so expect negative currents.
 * @author Bowserinator
 */
class ResistorTests {
    /**
     * Testing voltage and current values for a 1k
     * in series with 10 V source
     */
    @Test
    @DisplayName("1k ohm resistor in series w/ 10 V source")
    void r1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I = -10.0 / 1000.0;

        assertEquals(10.0, R1.getVoltage(), EPSILON);
        assertEquals(I, R1.getCurrent(), EPSILON);
        assertEquals(I, V1.getCurrent(), EPSILON);
    }

    /**
     * Testing voltage and current with 2 resistors in series with
     * a 10 V source.
     */
    @Test
    @DisplayName("1k ohm resistor in series w/ 10 V source and 500 ohm resistor")
    void r2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R2, 1, 2);
        circuit.addComponent(V1, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I = -10.0 / 1500.0;

        assertEquals(20.0 / 3.0, R1.getVoltage(), EPSILON);
        assertEquals(10.0 / 3.0, R2.getVoltage(), EPSILON);
        assertEquals(I, R1.getCurrent(), EPSILON);
        assertEquals(I, R2.getCurrent(), EPSILON);
        assertEquals(I, V1.getCurrent(), EPSILON);
    }

    /**
     * Testing voltage and current for 3 3k resistors in parallel
     * with a 10 V source. Voltage should be 10 V and current
     * should be 1/3rd for each resistor compared to test1
     */
    @Test
    @DisplayName("10 V source in parallel with 3 3k ohm resistors")
    void r3() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(3000);
        VirtualResistor R2 = new VirtualResistor(3000);
        VirtualResistor R3 = new VirtualResistor(3000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R2, 0, 1);
        circuit.addComponent(R3, 0, 1);
        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I = -10.0 / 1000.0;

        assertEquals(10.0, R1.getVoltage(), EPSILON);
        assertEquals(10.0, R2.getVoltage(), EPSILON);
        assertEquals(10.0, R3.getVoltage(), EPSILON);
        assertEquals(I / 3, R1.getCurrent(), EPSILON);
        assertEquals(I / 3, R2.getCurrent(), EPSILON);
        assertEquals(I / 3, R3.getCurrent(), EPSILON);
        // No check for V1 current because not supported in this configuration
    }

    /**
     * Voltage and current through parallel resistors that are not
     * all the same resistance.
     */
    @Test
    @DisplayName("10 V source in parallel with 1k, 2k, 3k resistor")
    void r4() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(2000);
        VirtualResistor R3 = new VirtualResistor(3000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R2, 0, 1);
        circuit.addComponent(R3, 0, 1);
        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10.0, R1.getVoltage(), EPSILON);
        assertEquals(10.0, R2.getVoltage(), EPSILON);
        assertEquals(10.0, R3.getVoltage(), EPSILON);
        assertEquals(-10.0 / 1000.0, R1.getCurrent(), EPSILON);
        assertEquals(-10.0 / 2000.0, R2.getCurrent(), EPSILON);
        assertEquals(-10.0 / 3000.0, R3.getCurrent(), EPSILON);
        // No check for V1 current because not supported in this configuration
    }

    /**
     * Voltage source in series with resistors, some are in series,
     * some are in parallel
     */
    @Test
    @DisplayName("10 V source + 1k ohm + (3 500 ohm in parallel)")
    void r5() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(500);
        VirtualResistor R3 = new VirtualResistor(500);
        VirtualResistor R4 = new VirtualResistor(500);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(R2, 1, 2);
        circuit.addComponent(R3, 1, 2);
        circuit.addComponent(R4, 1, 2);
        circuit.addComponent(V1, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I = -10.0 / (1000.0 + 500.0 / 3.0);

        assertEquals(10.0 * (1000.0 / (1000.0 + 500.0 / 3)), R1.getVoltage(), EPSILON);
        assertEquals(10.0 * (500.0 / 3 / (1000.0 + 500.0 / 3)), R2.getVoltage(), EPSILON);
        assertEquals(10.0 * (500.0 / 3 / (1000.0 + 500.0 / 3)), R3.getVoltage(), EPSILON);
        assertEquals(10.0 * (500.0 / 3 / (1000.0 + 500.0 / 3)), R4.getVoltage(), EPSILON);

        assertEquals(I, R1.getCurrent(), EPSILON);
        assertEquals(I / 3, R2.getCurrent(), EPSILON);
        assertEquals(I / 3, R3.getCurrent(), EPSILON);
        assertEquals(I / 3, R4.getCurrent(), EPSILON);
        assertEquals(I, V1.getCurrent(), EPSILON);
    }

    /**
     * Resistors in series with a positive and negative voltage source.
     */
    @Test
    @DisplayName("10 V source + 1k ohm + -5V source + 2k ohm (in series)")
    void r6() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(2000);
        VirtualResistor R3 = new VirtualResistor(0.001);

        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualVoltageSource V2 = new VirtualVoltageSource(5);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(V1, 1, 2);
        circuit.addComponent(R2, 2, 3);
        circuit.addComponent(V2, 4, 3);
        circuit.addComponent(R3, 4, 0);
        circuit.addComponent(new VirtualGround(), 0, 0); // Can't directly connect to voltage source, can overwrite supernode
        circuit.solve();

        double I = 5.0 / (3000.0);

        assertEquals(1000 * I, R1.getVoltage(), EPSILON);
        assertEquals(2000 * I, R2.getVoltage(), EPSILON);

        assertEquals(-I,  V1.getCurrent(), EPSILON);
        assertEquals(I,   V2.getCurrent(), EPSILON);
        assertEquals(-I,  R1.getCurrent(), EPSILON);
        assertEquals(-I,  R2.getCurrent(), EPSILON);
    }

    /**
     * A more complex circuit layout with multiple voltage
     * sources and resistors in a grid.
     * Nodes and values are labelled below.
     *
     *  1            2              3
     *  +-----WWWW---+-----WWWW-----+
     *  |    R1 1k   |      1k R2   |
     * [+] 10 V      Z 1k R3       [+] 15 V   V2
     * [-] V1        Z             [-]
     *  |            |              |
     *  +------------------------------||| Ground 0
     */
    @Test
    @DisplayName("T shaped configuration with 2 voltage sources")
    void r7() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualResistor R2 = new VirtualResistor(1000);
        VirtualResistor R3 = new VirtualResistor(1000);

        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualVoltageSource V2 = new VirtualVoltageSource(15);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(V2, 3, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(R2, 3, 2);
        circuit.addComponent(R3, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I_R1 = 0.001666666667;
        double I_R2 = 0.006666666667;
        double I_R3 = 0.008333333333;

        assertEquals(-1000 * I_R1, R1.getVoltage(), EPSILON);
        assertEquals(-1000 * I_R2, R2.getVoltage(), EPSILON);
        assertEquals(-1000 * I_R3, R3.getVoltage(), EPSILON);

        assertEquals(-I_R1, V1.getCurrent(), EPSILON);
        assertEquals(-I_R2, V2.getCurrent(), EPSILON);
        assertEquals(I_R1,  R1.getCurrent(), EPSILON);
        assertEquals(I_R2,  R2.getCurrent(), EPSILON);
        assertEquals(I_R3,  R3.getCurrent(), EPSILON);
    }
}
