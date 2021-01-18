package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;


// stuff like no ground, 1 element ,etc...
// impossible situations (ie ground - v - ground)
// disable component midway
// disabling components at beginning
// not a complete loop, just ends with ground
// disabling component, nullify it

class EdgeCaseTest {
    @Test
    @DisplayName("3 1 ohm in series 10 V, 2 0A current sources - No crash")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualResistor R3 = new VirtualResistor(1);
        VirtualCurrentSource CS1 = new VirtualCurrentSource(150);
        VirtualCurrentSource CS2 = new VirtualCurrentSource (150);
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

        // Assert that the thing doesn't crash
    }

}
