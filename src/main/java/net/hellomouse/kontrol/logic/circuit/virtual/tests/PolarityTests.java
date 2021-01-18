package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PolarityTest {
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

    @Test
    @DisplayName("Currents are correct polarity")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10.0 / 1000.0, circuit.getCurrentThrough(1, 0), EPSILON);
        assertEquals(R1.getCurrent(),  circuit.getCurrentThrough(0, 1), EPSILON);
        assertEquals(V1.getCurrent(), -circuit.getCurrentThrough(0, 1), EPSILON); // V1 nodes in opposite order = opposite current
    }

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

        assertEquals(-(9.3) / 101, R1.getCurrent(), EPSILON);
    }

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

    @Test
    @DisplayName("Current sources have correct polarity")
    void test5() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualCurrentSource CS1 = new VirtualCurrentSource(10);

        circuit.addComponent(CS1, 1, 0);
        circuit.addComponent(new VirtualResistor(1), 1, 2);
        circuit.addComponent(R1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10, R1.getCurrent(), EPSILON);
    }
}
