package net.hellomouse.kontrol.electrical.circuit.virtual.tests;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.DT;


/**
 * RLC Circuit tests
 * @author Bowserinator
 */
class RLCCircuitTests {
    @Test
    @DisplayName("Underdamped circuit: R = 10, L = 1.5, C = 0.01")
    void test1() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualCapacitor C1 = new VirtualCapacitor(0.01);
        VirtualInductor L1 = new VirtualInductor(1.5);
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

        ArrayList<Double> R1Voltage = new ArrayList<>();

        for (int i = 0; i < 3 / DT; i++) {
            circuit.tick();
            circuit.solve();
            R1Voltage.add(R1.getVoltage());
        }

        System.out.println("Expected: A sinusoid that goes up, then down and decays\n");
        graph(R1Voltage, 1);
    }

    @Test
    @DisplayName("More underdamped circuit: R = 0.1, L = 1.5, C = 0.01")
    void test2() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualCapacitor C1 = new VirtualCapacitor(0.01);
        VirtualInductor L1 = new VirtualInductor(1.5);
        VirtualResistor R1 = new VirtualResistor(0.1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(new VirtualResistor(0.001), 3, 4);
        circuit.addComponent(L1, 4, 5);
        circuit.addComponent(R1, 5, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        ArrayList<Double> R1Voltage = new ArrayList<>();

        for (int i = 0; i < 5 / DT; i++) {
            circuit.tick();
            circuit.solve();
            R1Voltage.add(R1.getVoltage());
        }

        System.out.println("Expected: A sinusoid that goes up, then down and decays slowly\n");
        graph(R1Voltage, 1);
    }

    @Test
    @DisplayName("Underdamped circuit with 0.7 V diode: R = 1, L = 1.5, C = 0.01")
    void test3() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualCapacitor C1 = new VirtualCapacitor(0.01);
        VirtualInductor L1 = new VirtualInductor(1.5);
        VirtualResistor R1 = new VirtualResistor(1);
        VirtualVoltageSource V1 = new VirtualVoltageSource(10);
        VirtualDiode D1 = new VirtualDiode(0.7);

        circuit.addComponent(V1, 1, 0);
        circuit.addComponent(new VirtualResistor(0.001), 1, 2);
        circuit.addComponent(C1, 2, 3);
        circuit.addComponent(new VirtualResistor(0.001), 3, 4);
        circuit.addComponent(L1, 4, 5);
        circuit.addComponent(R1, 5, 6);
        circuit.addComponent(D1, 6, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();

        ArrayList<Double> R1Voltage = new ArrayList<>();

        for (int i = 0; i < 5 / DT; i++) {
            circuit.tick();
            circuit.solve();
            R1Voltage.add(R1.getVoltage());
        }

        System.out.println("A graph that curves down at the beginning then asymptotically approaches zero\n");
        graph(R1Voltage, 1);
    }

    @Test
    @DisplayName("Overdamped circuit: R = 10, L = 1.5, C = 1")
    void test4() {
        VirtualCircuit circuit = new VirtualCircuit();

        VirtualCapacitor C1 = new VirtualCapacitor(1);
        VirtualInductor L1 = new VirtualInductor(1.5);
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

        ArrayList<Double> R1Voltage = new ArrayList<>();

        for (int i = 0; i < 100 / DT; i++) {
            circuit.tick();
            circuit.solve();
            R1Voltage.add(R1.getVoltage());
        }

        System.out.println("A graph that goes asymptotically to zero without crossing zero\n");
        graph(R1Voltage, 16);
    }

    /**
     * Draw an ASCII graph given a list of value to plot, where index is the x axis and value is scaled
     * to a fixed graph height and size
     * @param values Values to plot
     * @param keep Keep every nth value, use to compress graph horizontally
     */
    private void graph(ArrayList<Double> values, int keep) {
        final int GRAPH_HEIGHT = 21;
        final int GRAPH_WIDTH = values.size() / keep + 1;
        String[] graph = new String[GRAPH_HEIGHT];

        // Generate graph
        for (int y = 0; y < GRAPH_HEIGHT; y++) {
            graph[y] = y == GRAPH_HEIGHT / 2 ?
                String.join("", Collections.nCopies(GRAPH_WIDTH, "-")) :
                String.join("", Collections.nCopies(GRAPH_WIDTH, " "));
        }

        // Plot values. May be inefficient but we're not plotting large plots
        double maxValue = Collections.max(values.stream().map(Math::abs).collect(Collectors.toList()));

        for (int i = 0; i < values.size(); i++) {
            if (i % keep != 0)
                continue;

            double value = values.get(i);
            int scaledHeight = (int)Math.round(value / (maxValue * 2) * GRAPH_HEIGHT + (float)GRAPH_HEIGHT / 2);

            char[] chars = graph[scaledHeight].toCharArray();
            chars[i / keep] = 'X';
            graph[scaledHeight] = String.valueOf(chars);
        }

        for (String line : graph)
            System.out.println(line);
    }
}
