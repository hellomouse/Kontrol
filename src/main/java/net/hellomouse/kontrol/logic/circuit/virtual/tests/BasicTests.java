package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


class BasicTest {
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
        assertEquals(-10.0, L1.getVoltage(), EPSILON);
        assertEquals(D1.getVoltage(), 0.0, EPSILON);
    }

    @Test
    @DisplayName("Resistor getEnergy() works properly")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10 * 10 / 1000.0, R1.getEnergy(), EPSILON);
    }

    @Test
    @DisplayName("Resistor getEnergy() works properly")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1000);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(10 * 10 / 1000.0, R1.getEnergy(), EPSILON);
    }

    @Test
    @DisplayName("Capacitor getEnergy() works properly")
    void test4() {
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

        for (int i = 0; i < 10; i++) {
            circuit.tick();
            circuit.solve();
        }
        assertEquals(0.5 * 0.01 * 10 * 10, C1.getEnergy(), EPSILON);
    }

    @Test
    @DisplayName("Inductor getEnergy() works properly")
    void test5() {
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

    @Test
    @DisplayName("Voltage and diodes return UNKNOWN_ENERGY for getEnergy()")
    void test6() {
        VirtualCircuit circuit = new VirtualCircuit();
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualDiode D1 = new VirtualDiode(0.7);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(new VirtualResistor(1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, V1.getEnergy(), EPSILON);
        assertEquals(VirtualCircuitConstants.UNKNOWN_ENERGY, D1.getEnergy(), EPSILON);
    }
}
