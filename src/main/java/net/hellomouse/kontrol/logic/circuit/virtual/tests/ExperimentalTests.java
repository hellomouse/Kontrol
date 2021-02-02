package net.hellomouse.kontrol.logic.circuit.virtual.tests;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;
import static net.hellomouse.kontrol.logic.circuit.virtual.tests.TestConstants.ONE_TAU;


/**
 * Tests of edge case circuits., ie, extreme values, or unusual
 * circuit layouts that might cause problems.
 * @author Bowserinator
 */
class ExperimentalTests {
    private static final ArrayList<Integer> chargeTimes = new ArrayList<>(Arrays.asList(3, 5, 8, 10, 12, 15, 25, 50, 75, 100));

    @Test
    @DisplayName("Capacitor numeric integration - charging")
    void test1() {
        System.out.println("Capacitor - charging");
        for (int i : chargeTimes)
            capacitorChargeTest(i);
        System.out.println(" ");
    }

    @Test
    @DisplayName("Capacitor numeric integration - discharging")
    void test2() {
        System.out.println("Capacitor - discharging");
        for (int i : chargeTimes)
            capacitorDischargeTest(i);
        System.out.println(" ");
    }

    @Test
    @DisplayName("Print names of all components")
    void test3() {
        ArrayList<AbstractVirtualComponent> comps = new ArrayList<>();

        comps.add(new VirtualResistor(1));
        comps.add(new VirtualVoltageSource(1));
        comps.add(new VirtualCapacitor(1));
        comps.add(new VirtualCurrentSource(1));
        comps.add(new VirtualDiode(1));
        comps.add(new VirtualFixedNode(1));
        comps.add(new VirtualGround());
        comps.add(new VirtualInductor(1));

        for (AbstractVirtualComponent comp : comps) {
            VirtualCircuit circuit = new VirtualCircuit();
            circuit.addComponent(comp, 0, 1);
            circuit.addComponent(new VirtualResistor(1), 1, 0);
            circuit.solve();
            System.out.println(comp);
        }
    }

    private void capacitorChargeTest(int n) {
        VirtualCircuit circuit = new VirtualCircuit();

        double R = 10;
        double C = 0.01;

        VirtualResistor R1 = new VirtualResistor(R - 1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(C);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(R1, 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(R2, 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        for (int i = 0; i < n; i++) {
            circuit.tick();
            circuit.solve();
        }

        System.out.println(". Error for " + n + " times: " + (10 - 10 * Math.exp(-n * DT / R / C) - C1.getVoltage()));
    }

    private void capacitorDischargeTest(int n) {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualResistor R1 = new VirtualResistor(1);
        VirtualResistor R2 = new VirtualResistor(1);
        VirtualCapacitor C1 = new VirtualCapacitor(n * DT / 2);

        circuit.addComponent(R1, 0, 1);
        circuit.addComponent(C1, 1, 2);
        circuit.addComponent(R2, 2, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);

        C1.setVoltage(-10.0);

        circuit.solve();

        for (int i = 0; i < n; i++) {
            circuit.tick();
            circuit.solve();
        }

        System.out.println(". Error for " + n + " times: " + (10 * (1 - ONE_TAU) + C1.getVoltage()));
    }
}
