package net.hellomouse.kontrol.logic.circuit.virtual;

import net.hellomouse.kontrol.logic.circuit.virtual.components.AbstractVirtualComponent;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualCapacitor;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualInductor;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class VirtualCondition {
    public enum Condition {
        VOLTAGE_DIFFERENCE, RESISTANCE, CURRENT, FIXED_VOLTAGE
    };

    // Positive node = node1
    // Negative node = node2
    public final int node1, node2;
    public double value;
    public Condition type;

    public VirtualCondition(int node1, int node2, double value, Condition type) {
        this.node1 = node1;
        this.node2 = node2;
        this.value = value;
        this.type = type;
    }


    // --- Computation for each condition --- \\
    // The following conditions must be executed in the order
    // they are declared in this file

    // Note: we could declare a solver for each condition, but
    // I don't want to sink any deeper into OOP hell

    public static void KCLCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions, boolean steadyState) {
        if (components == null)
            return;
        for (AbstractVirtualComponent comp : components) {
            VirtualCondition condition = comp.getCondition();

            double invR = 1 / condition.value;
            boolean shouldSolve = condition.type == Condition.RESISTANCE;

            if (comp.isDisabled() || (steadyState && comp.doesNumericIntegration())) {
                // TODO: dont hard code
                if (steadyState && comp instanceof VirtualInductor)
                    invR = 1e9;
                else
                    invR = 1e-9;
                shouldSolve = true;
            }

            if (shouldSolve) {
                int[] startingIds = {condition.node1, condition.node2};
                for (int nodeId : startingIds) {
                    // (N1 - N2) / R = current into node. We add the negative sign to N2
                    // later on, for now this expression is (N1 + N2) / R
                    addToMatrix(matrix, nodeId, condition.node1, invR);
                    addToMatrix(matrix, nodeId, condition.node2, invR);
                }
            }
        }

        for (int nodeId = 0; nodeId < matrix.numRows(); nodeId++) {
            // KCL condition: sum all currents = 0
            solutions.set(nodeId, 0, 0.0);
            // Flip sign as mentioned above
            setMatrix(matrix, nodeId, nodeId, -getMatrix(matrix, nodeId, nodeId));
        }
    }

    public static void currentSourceCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions, boolean steadyState) {
        if (components == null)
            return;

        for (AbstractVirtualComponent comp : components) {
            VirtualCondition condition = comp.getCondition();

            if (comp.isDisabled())
                continue;

            // Steady state: inductor is short
            if (steadyState && comp instanceof VirtualInductor)
                continue;

            // Current source from N1 to N2
            // Ignoring the branch the current source is on
            // Total current out of N1 is now = current source
            // Total current out of N2 is now = -current source

            // We assume no more than 1 resistor connects to each end of the terminal
            // (Following rule all non-resistors must be wrapped in series w/ a resistor)

            solutions.set(condition.node1, 0, solutions.get(condition.node1, 0) + condition.value);
            solutions.set(condition.node2, 0, solutions.get(condition.node2, 0) - condition.value);
        }
    }

    public static void voltageDifferenceCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions, boolean steadyState) {
        if (components == null)
            return;

        final int nodeCount = matrix.numRows();
        for (AbstractVirtualComponent comp : components) {
            VirtualCondition condition = comp.getCondition();

            if (comp.isDisabled())
                continue;

            // Steady state: capacitor is open circuit
            if (steadyState && comp instanceof VirtualCapacitor)
                continue;

            // Voltage difference: supernode definition
            // > condition.value = voltage difference

            // node1 - node2 = voltage

            // Overwrite original rows by summing the two to get KCL equation
            solutions.set(condition.node2, 0, solutions.get(condition.node1, 0) + solutions.get(condition.node2, 0));
            solutions.set(condition.node1, 0, condition.value);

            for (int i = 0; i < nodeCount; i++) {
                matrix.set(condition.node2, i, matrix.get(condition.node1, i) + matrix.get(condition.node2, i));

                double supernodeVal = 0.0;
                if (i == condition.node1)
                    supernodeVal = 1.0;
                else if (i == condition.node2)
                    supernodeVal = -1.0;
                matrix.set(condition.node1, i, supernodeVal);
            }
        }
    }

    public static void fixedNodeCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions) {
        if (components == null)
            return;

        final int nodeCount = matrix.numRows();
        for (AbstractVirtualComponent comp : components) {
            VirtualCondition condition = comp.getCondition();

            if (comp.isDisabled())
                continue;

            // Fixed voltage: just use [node] = [voltage]


            solutions.set(condition.node1, 0, condition.value);
            for (int i = 0; i < nodeCount; i++) {
                matrix.set(condition.node1, i, i == condition.node1 ? 1.0 : 0.0);
            }
        }
    }


    // --- Helper for each condition --- \\

    /* Returns ArrayList w/ Doubles filled with [size] zeroes */
    // TODO: move to VirutalCirucit or something lol
    public static ArrayList<Double> getEmptyRow(int size) {
        ArrayList<Double> row = new ArrayList<>(Arrays.asList(new Double[size]));
        Collections.fill(row, 0.0);
        return row;
    }

    /* Increments matrix[i][j] by value */
    public static void addToMatrix(SimpleMatrix matrix, int i, int j, double value) {
        setMatrix(matrix, i, j, getMatrix(matrix, i, j) + value);
    }

    public static double getMatrix(SimpleMatrix matrix, int i, int j) {
        return matrix.get(i, j);
    }

    public static void setMatrix(SimpleMatrix matrix, int i, int j, double value) {
        matrix.set(i, j, value);
    }


    public String toString() {
        return "Nodes: " + node1 + " to " + node2 + "   Value: " + value + " Type: " + type;
    }
}
