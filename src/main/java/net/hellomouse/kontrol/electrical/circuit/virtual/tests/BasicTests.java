package net.hellomouse.kontrol.electrical.circuit.virtual.tests;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.electrical.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests for basic functionality, like getVoltage(),
 * getEnergy(), etc...
 * @author Bowserinator
 */
class BasicTests {
    /**
     * A circuit consisting of a:
     * - Voltage source (10 V)
     * - Resistor (1 kilo-ohm)
     * - Capacitor (1 mF)
     * - Inductor (1 mH)
     * - Diode (V_Fwd = 0.7)
     * in series. There should be no current, so the only voltage
     * is across the inductor and voltage source = 10 V.
     *
     * getVoltage() for most components uses the nodal voltages, so this
     * is a test of solving accuracy. Voltage sources and capacitors return the
     * voltage value of the source. At initial state the capacitor = 0 V.
     */
    @Test
    @DisplayName("getVoltage() works properly")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualCapacitor C1 = new VirtualCapacitor(0.001);
        VirtualInductor L1 = new VirtualInductor(0.001);
        VirtualDiode D1 = new VirtualDiode(0.7);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 4);
        circuit.addComponent(L1, 4, 5);
        circuit.addComponent(new VirtualResistor(1), 5, 6);
        circuit.addComponent(D1, 6, 7);
        circuit.addComponent(new VirtualResistor(1), 7, 0);

        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10.0, V1.getVoltage(), EPSILON);
        assertEquals(0.0, R1.getVoltage(), EPSILON);
        assertEquals(0.0, C1.getVoltage(), EPSILON);

        assertEquals(-9.3, L1.getVoltage(), EPSILON);
        assertEquals(0.7, D1.getVoltage(), EPSILON);
    }

    /**
     * A simple test of getPower(), where P = VI. This formula is
     * used for all components, so if it doesn't work in this simple
     * resistor case, it won't work at all.
     */
    @Test
    @DisplayName("Resistor getPower() works properly")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10 * 10 / 1000.0, R1.getPower(), EPSILON); // P = V^2 / R = VI
    }

    /**
     * Capacitor energy is 1/2 CV^2, where C is capacitance and
     * V is voltage across capacitor.
     *
     * The circuit below has a time constant
     * of 1 ohm * 0.01 F = 0.01s. We simulate the circuit for 0.5s, which is 50x
     * the RC constant to make sure the capacitor has reached steady state
     * where it behaves like a open circuit, so it's voltage = 10 V
     */
    @Test
    @DisplayName("Capacitor getEnergy() works properly")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(0.01);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < 0.5 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }
        assertEquals(0.5 * 0.01 * 10 * 10, C1.getEnergy(), EPSILON);
    }

    /**
     * Inductor energy is 1/2 LI^2, where L is inductance and
     * I is current through inductor.
     *
     * The circuit below has a time constant
     * of 1 H / 2 ohm = 0.5 s. We simulate the circuit for 10s, which is 20x
     * the RL constant to make sure the inductor has reached steady state
     * where it behaves like a wire, so it's current is 10 V / 2 = 5 A
     */
    @Test
    @DisplayName("Inductor getEnergy() works properly")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2  =new VirtualResistor(1);
        VirtualInductor L1 = new VirtualInductor(1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(L1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < (10 / DT); i++) {
            circuit.tick();
            circuit.solve();
        }
        assertEquals(0.5 * 25, L1.getEnergy(), EPSILON);
    }

    /**
     * These components don't store energy. They should return
     * UNKNOWN_ENERGY when asked.
     */
    @Test
    @DisplayName("Voltage sources, resistors, fixed nodes and diodes return UNKNOWN_ENERGY for getEnergy()")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualDiode D1 = new VirtualDiode(0.7);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualFixedNode N1 = new VirtualFixedNode(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(N1, 2, 2);
        circuit.solve();

        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, V1.getEnergy(), EPSILON);
        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, R1.getEnergy(), EPSILON);
        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, D1.getEnergy(), EPSILON);
        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, N1.getEnergy(), EPSILON);
    }

    /**
     * Fixed node actually fixes the voltage
     */
    @Test
    @DisplayName("Fixed nodes actually have fixed voltage")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualDiode D1 = new VirtualDiode(0.7);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualFixedNode N1 = new VirtualFixedNode(5);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.addComponent(N1, 2, 2);
        circuit.solve();

        assertEquals(5.0, circuit.getNodalVoltage(2), EPSILON);
        assertEquals(-5.0, R1.getVoltage(), EPSILON);
    }
}
