package net.hellomouse.kontrol.electrical.circuit.virtual.tests;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.electrical.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Checks polarity of all elements are implemented correctly.
 * @author Bowserinator
 */
class PolarityTests {
    /**
     * Node1 = positive terminal
     * Node2 = negative terminal
     *
     * Thus, node1 voltage > node2 voltage. If node2 is grounded,
     * node1 voltage should equal voltage source value.
     */
    @Test
    @DisplayName("Voltage source behaves with correct polarity")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10.0, circuit.getNodalVoltage(1), EPSILON);
    }

    /**
     * Get current through computes current flowing from node1 to node2 as positive.
     * This test verifies that, as well as components inserted with reversed node order
     * like V1 below have correct current polarity as well (for V1, current should be opposite
     * of current through 0 to 1 as it was inserted 1 to 0)
     */
    @Test
    @DisplayName("Currents are correct polarity - getCurrentThrough")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(-10.0 / 1000.0, circuit.getCurrentThrough(1, 0), EPSILON);
        assertEquals(R1.getCurrent(),  circuit.getCurrentThrough(0, 1), EPSILON);
        assertEquals(V1.getCurrent(), -circuit.getCurrentThrough(0, 1), EPSILON); // V1 nodes in opposite order = opposite current
    }

    /**
     * Node1 = positive terminal
     * Node2 = negative terminal
     *
     * Thus a diode from 0 to 1 will allow current to flow from 0 to 1, but not 1 to 0.
     * We verify a simple diode-resistor circuit below has correct current flowing through
     * the resistor.
     */
    @Test
    @DisplayName("Diodes facing correct way")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(100);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualDiode D1 = new VirtualDiode(0.7);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(R2, 3, 0);

        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(9.3 / 101, R1.getCurrent(), EPSILON);
    }

    /**
     * Same as test3, but reversing the direction of the diode.
     * No current should flow now.
     */
    @Test
    @DisplayName("Diodes facing wrong way")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(100);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualDiode D1 = new VirtualDiode(0.7);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(D1, 3, 2);
        circuit.addComponent(R2, 3, 0);

        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(0.0, R1.getCurrent(), EPSILON);
    }

    /**
     * Current sources create current from node1 to node2, this
     * can be checked by measuring current through R1
     */
    @Test
    @DisplayName("Current sources have correct polarity")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualCurrentSource CS1 = new VirtualCurrentSource(10);

        circuit.addComponent(CS1, 0, 1);
        circuit.addComponent(new VirtualResistor(1), 1, 2);
        circuit.addComponent(R1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10, R1.getCurrent(), EPSILON);
    }
}
