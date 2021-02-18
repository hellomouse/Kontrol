package net.hellomouse.kontrol.electrical.circuit.virtual;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualInductor;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.*;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.OPEN_CIRCUIT_R;
import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.SHORT_CIRCUIT_R;

/**
 * Handling of all condition interfaces (does all the matrix manipulations)
 * @author Bowserinator
 */
public class VirtualCondition {
    // Correspond to all condition interfaces, used as key in component cache
    public enum Condition { VOLTAGE_DIFFERENCE, RESISTANCE, CURRENT, FIXED_VOLTAGE, CUSTOM }

    /**
     * Solve all KCL conditions for resistors.
     * @param components All components, not just resistors, as sometimes other components can be treated as resistors
     * @param matrix Left side matrix
     * @param solutions Right side matrix
     * @param steadyState Solving for steady state?
     */
    public static void KCLCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions, boolean steadyState) {
        if (components == null) return;

        for (AbstractVirtualComponent comp : components) {
            double invR = 0.0; // Arbitrary initial value, should always be overwritten
            boolean shouldSolve = comp instanceof IResistanceCondition;

            // Set invR for a resistor
            if (shouldSolve)
                invR = 1 / ((IResistanceCondition)comp).getResistance();

            // High impedance
            if (comp.isHiZ()) {
                invR = 1 / comp.getHiZR(); // invR is very low because R is very high
                shouldSolve = true;
            }

            // Non-resistor that behaves like one right now
            if (steadyState && comp.doesNumericIntegration()) {
                invR = comp instanceof VirtualInductor ? OPEN_CIRCUIT_R : SHORT_CIRCUIT_R;
                shouldSolve = true;
            }

            if (shouldSolve) {
                int[] startingIds = {comp.getNode1(), comp.getNode2()};
                for (int nodeId : startingIds) {
                    // (N1 - N2) / R = current into node. We add the negative sign to N2
                    // later on, for now this expression is (N1 + N2) / R
                    addToMatrix(matrix, nodeId, comp.getNode1(), invR);
                    addToMatrix(matrix, nodeId, comp.getNode2(), invR);
                }
            }
        }

        for (int nodeId = 0; nodeId < matrix.numRows(); nodeId++) {
            // KCL condition: sum all currents = 0
            solutions.set(nodeId, 0, 0.0);
            // Flip sign as mentioned above
            matrix.set(nodeId, nodeId, -matrix.get(nodeId, nodeId));
        }
    }

    /**
     * Solve for all current source conditions
     * @param components Only components that implement ICurrentCondition. This is not checked!
     * @param solutions Right hand matrix
     * @param steadyState Solve for steady state?
     */
    public static void currentSourceCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix solutions, boolean steadyState) {
        if (components == null) return;

        for (AbstractVirtualComponent comp : components) {
            // Treated as a resistor, ignore now
            if (comp.isHiZ())
                continue;

            // Steady state: ignore resistors and capacitors
            if (steadyState && comp.doesNumericIntegration())
                continue;

            // Current source from N1 to N2
            // Ignoring the branch the current source is on
            // Total current out of N1 is now = current source
            // Total current out of N2 is now = -current source

            solutions.set(comp.getNode1(), 0, solutions.get(comp.getNode1(), 0) + comp.getCurrent());
            solutions.set(comp.getNode2(), 0, solutions.get(comp.getNode2(), 0) - comp.getCurrent());
        }
    }

    /**
     * Solve all voltage differences
     * @param components Only components that implement IVoltageDifferenceCondition. This is not checked!
     * @param matrix Left hand matrix
     * @param solutions Right hand matrix
     * @param steadyState Solve for steady state?
     */
    public static void voltageDifferenceCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions, boolean steadyState) {
        if (components == null) return;

        final int nodeCount = matrix.numRows();
        for (AbstractVirtualComponent comp : components) {
            // Treated as a resistor, ignore now
            if (comp.isHiZ())
                continue;

            // Voltage difference: supernode definition
            // > condition.value = voltage difference
            // node1 - node2 = voltage

            // Overwrite original rows by summing the two to get KCL equation
            solutions.set(comp.getNode2(), 0, solutions.get(comp.getNode1(), 0) + solutions.get(comp.getNode2(), 0));
            solutions.set(comp.getNode1(), 0, comp.getVoltage());

            for (int i = 0; i < nodeCount; i++) {
                matrix.set(comp.getNode2(), i, matrix.get(comp.getNode1(), i) + matrix.get(comp.getNode2(), i));

                double supernodeVal = 0.0;
                if (i == comp.getNode1())
                    supernodeVal = 1.0;
                else if (i == comp.getNode2())
                    supernodeVal = -1.0;
                matrix.set(comp.getNode1(), i, supernodeVal);
            }
        }
    }

    /**
     * Solve fixed nodal voltages
     * @param components Only components that implement IFixedVoltageCondition. This is not checked!
     * @param matrix Left hand matrix
     * @param solutions Right hand matrix
     */
    public static void fixedNodeCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions) {
        if (components == null)
            return;

        final int nodeCount = matrix.numRows();
        for (AbstractVirtualComponent comp : components) {
            // Ignore disabled fixed nodes
            if (comp.isDisabled())
                continue;

            // Fixed voltage: just use [node] = [voltage]
            // only node1 of the component is used.
            solutions.set(comp.getNode1(), 0, comp.getVoltage());
            for (int i = 0; i < nodeCount; i++)
                matrix.set(comp.getNode1(), i, i == comp.getNode1() ? 1.0 : 0.0);
        }
    }

//    /**
//     * Solve custom matrix solutions
//     * @param components Only components that implement ICustomCondition. This is not checked!
//     * @param matrix Left hand matrix
//     * @param solutions Right hand matrix
//     */
//    public static void customCondition(ArrayList<AbstractVirtualComponent> components, SimpleMatrix matrix, SimpleMatrix solutions) {
//        if (components == null)
//            return;
//
//        for (AbstractVirtualComponent comp : components)
//            ((ICustomCondition)(comp)).modifyMatrix(matrix, solutions);
//    }

    /**
     * Increment matrix[i][j] by value
     * @param matrix Matrix
     * @param i Row
     * @param j Col
     * @param value How much to increment, negative to decrement
     */
    private static void addToMatrix(SimpleMatrix matrix, int i, int j, double value) {
        matrix.set(i, j, matrix.get(i, j) + value);
    }

    /**
     * Returns if component belongs into a bin designated by key. This function is used
     * for mapping a component to (possibly multiple) condition enums
     * @param component Component to check
     * @param key Enum to check
     * @return Does component belong in a bin for key?
     */
    public static boolean fitsKey(AbstractVirtualComponent component, Condition key) {
        switch (key) {
            case VOLTAGE_DIFFERENCE: return component instanceof IVoltageDifferenceCondition;
            case RESISTANCE: return component instanceof IResistanceCondition;
            case CURRENT: return component instanceof ICurrentCondition;
            case FIXED_VOLTAGE: return component instanceof IFixedVoltageCondition;
            case CUSTOM: return component instanceof ICustomCondition;
        }
        throw new IllegalStateException("Enum " + key + " was not found (Condition handler missing?)");
    }

    private VirtualCondition() {}
}
