package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * RLC Circuit tests
 * @author Bowserinator
 */
class RLCCircuitTests {
    @Test
    @DisplayName("Overdamped circuit: R = 10, L = 0.1, C = 0.1")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualCapacitor C1 = new VirtualCapacitor(0.01);
        VirtualInductor L1 = new VirtualInductor(0.15);
        VirtualResistor R1 = new VirtualResistor(10);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(new VirtualResistor(0.001), 3, 4);
        circuit.addComponent(L1, 4, 5);
        circuit.addComponent(R1, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        // TODO
        // Steady state is correct
        // [0.0, 9.999999999999998, 9.999999999989999, 1.0000997616784776E-7, 9.999997616794777E-8, 9.999997615794778E-8]

        for (int i = 0; i < 5 / DT; i++) {
            circuit.tick();
            circuit.solve();
        }


        double I = -(10.0 - 1.4) / 1000.0;
        assertEquals(I, R1.getCurrent(), EPSILON);
        assertEquals(-10 + 1.4, R1.getVoltage(), EPSILON);
    }
}
