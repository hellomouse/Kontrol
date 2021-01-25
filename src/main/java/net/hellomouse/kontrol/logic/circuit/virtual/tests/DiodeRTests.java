package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Diode + resistor circuit tests.
 * @author Bowserinator
 */
class DiodeRTests {
    /**
     * 10 V source, 2 diodes and a 1 kilo-ohm resistor in series. The diodes
     * each have a 0.7 V forward voltage and face in the correct direction.
     *
     * We expect current to flow (10 V - 1.4 V) / (1k ohm) through all components.
     * R1 should have 10 - 1.4 = 8.6 V across, but due to matrix solver precision limitations
     * the final result is slightly larger than EPSILON so the leeway has been raised.
     * The equations for the matrix appeared to be correct after analysis.
     */
    @Test
    @DisplayName("2 Diodes in same directions, 10 V")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualDiode D1 = new VirtualDiode(0.7);
        VirtualDiode D2 = new VirtualDiode(0.7);
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(new VirtualResistor(0.001), 3, 4);
        circuit.addComponent(D2, 4, 5);
        circuit.addComponent(R1, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        double I = -(10.0 - 1.4) / 1000.0;
        assertEquals(I, D1.getCurrent(), EPSILON);
        assertEquals(I, D2.getCurrent(), EPSILON);
        assertEquals(I, R1.getCurrent(), EPSILON);
        assertEquals(-10 + 1.4, R1.getVoltage(), EPSILON);
    }

    /**
     * Same test as test1, but we flip the polarity on one of the diodes.
     * Thus, no current should flow.
     *
     * The voltage across R1 is 0 as a consequence. Note there is a 5V
     * drop across both diodes, as they are modeled as a very high value
     * resistor.
     */
    @Test
    @DisplayName("2 Diodes in opposite directions, 10 V")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualDiode D1 = new VirtualDiode(0.7);
        VirtualDiode D2 = new VirtualDiode(0.7);
        VirtualResistor R1 = new VirtualResistor(100);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(D1, 1, 2);
        circuit.addComponent(D2, 3, 2);
        circuit.addComponent(R1, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(0.0, D1.getCurrent(), EPSILON);
        assertEquals(0.0, D2.getCurrent(), EPSILON);
        assertEquals(0.0, R1.getCurrent(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
    }
}
