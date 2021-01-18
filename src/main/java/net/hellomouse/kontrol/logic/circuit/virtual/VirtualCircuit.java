package net.hellomouse.kontrol.logic.circuit.virtual;

import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class VirtualCircuit {
    // All components
    private final ArrayList<AbstractVirtualComponent> components = new ArrayList<>();
    // NodeID: All components that connect to that node
    private final HashMap<Integer, ArrayList<AbstractVirtualComponent>> nodeMap = new HashMap<>();
    // Components that use numeric integration
    private final ArrayList<AbstractVirtualComponent> divergentComponents = new ArrayList<>();
    // Components that are non-linear, such as diodes
    private final ArrayList<AbstractVirtualComponent> nonLinearComponents = new ArrayList<>();
    // Components that need to tick() when this.tick() is called
    private final ArrayList<AbstractVirtualComponent> requireTickComponents = new ArrayList<>();

    // Set of unique nodeIDs
    private final TreeSet<Integer> uniqueNodes = new TreeSet<>();
    // Solutions for voltages at every node
    private ArrayList<Double> nodalVoltages = new ArrayList<>();
    // Solutions for voltages at every node at steady state
    private ArrayList<Double> steadyStateNodalVoltages = new ArrayList<>();
    // Does circuit contain a ground?
    private boolean containsGround = false;


    /* Add a new component, connecting to node1 and node2 */
    public void addComponent(AbstractVirtualComponent component, int node1, int node2) {
        uniqueNodes.add(node1);
        uniqueNodes.add(node2);

        component.setNodes(node1, node2);
        component.setCircuit(this);
        components.add(component);

        nodeMap.computeIfAbsent(node1, k -> new ArrayList<>());
        nodeMap.get(node1).add(component);

        if (node1 != node2) {
            nodeMap.computeIfAbsent(node2, k -> new ArrayList<>());
            nodeMap.get(node2).add(component);
        }

        // Checks for circuit
        if (component instanceof VirtualGround)
            containsGround = true;
        if (component.requireTicking())
            requireTickComponents.add(component);

        // Add special components
        if (component.doesNumericIntegration())
            divergentComponents.add(component);
        if (component.isNonLinear())
            nonLinearComponents.add(component);
    }


    // --- Simulation --- \\

    /**
     * Solves the circuit given the components.
     *
     * Caveats:
     * - All nodes ID must be used up to the max. ie, if the highest nodeId is 3, then
     *   nodes 0, 1, 2, and 3 must all exist
     * - Non-resistors must only connect to resistors (Ie, no directly chaining voltage sources)
     */
    public void solve() {
        if (!containsGround) { // No ground = refuse to solve
            nodalVoltages = VirtualCondition.getEmptyRow(uniqueNodes.size());
            return;
        }
        nodalVoltages = solveHelper(false);
        recomputeSpecialCases();
    }

    /**
     * Fixes divergence in numeric integration in integrating components
     * (capacitors and inductors), as well as solve non-linear components
     * (diodes).
     */
    private void recomputeSpecialCases() {
        boolean recompute = false;

        // Divergence checking is required -- check if surpassed steady state
        if (divergentComponents.size() > 0) {
            // Use cached steady state result, or solve for it
            ArrayList<Double> steadyState = steadyStateNodalVoltages.size() == 0 ?
                    solveHelper(true) :
                    steadyStateNodalVoltages;
            if (steadyStateNodalVoltages.size() == 0)
                steadyStateNodalVoltages = steadyState;

            for (AbstractVirtualComponent comp : divergentComponents) {
                VirtualCondition condition = comp.getCondition();

                // Capacitors: calculate voltage across, cannot exceed steady state value
                if (comp instanceof VirtualCapacitor) {
                    double SS_voltage = steadyState.get(condition.node1) - steadyState.get(condition.node2);
                    if (Math.abs(comp.getVoltage()) > Math.abs(SS_voltage)) {
                        ((VirtualCapacitor) comp).setVoltage(SS_voltage);
                        recompute = true;
                    }
                }

                // Inductors: calculate current across, cannot exceed steady state value
                else if (comp instanceof VirtualInductor) {
                    double SS_voltage = steadyState.get(condition.node1) - steadyState.get(condition.node2);
                    double SS_current = SS_voltage * 1e9; // TODO hardcoded constant
                    if (Math.abs(comp.getCurrent()) > Math.abs(SS_current)) {
                        ((VirtualInductor) comp).setCurrent(SS_current);
                        recompute = true;
                    }
                }
            }
        }

        // Diode computations
        if (nonLinearComponents.size() > 0) {
            for (AbstractVirtualComponent comp : nonLinearComponents) {
                VirtualCondition condition = comp.getCondition();

                if (comp instanceof VirtualDiode) {
                    // Enable diode if:
                    // - Current flowing correct way (+ to -) & forward voltage reached
                    // - We do not implement V_breakdown
                    double V = comp.getVoltage();
                    double I = comp.getCurrent();

                    recompute = true; // Always recompute diodes
                    steadyStateNodalVoltages.clear(); // Diodes may alter steady state voltages, clear cache
                    comp.setDisabled(!(I < 0 && Math.abs(V) >= ((VirtualDiode)comp).getVForward()));
                }
            }
        }

        if (recompute)
            nodalVoltages = solveHelper(false);
    }

    /**
     * Matrix solver for current circuit state
     * @param steadyState Should it solve for steady state condition?
     * @return Nodal voltage ArrayList
     */
    private ArrayList<Double> solveHelper(boolean steadyState) {
        // Compute number of unique node IDs
        int nodeCount = uniqueNodes.size();
        if (nodeCount < 2)
            return VirtualCondition.getEmptyRow(nodeCount);

        // Helper data for generating final matrix
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>();
        ArrayList<Double> solutions = new ArrayList<>();
        HashMap<VirtualCondition.Condition, ArrayList<AbstractVirtualComponent>> componentTypeMap = new HashMap<>();

        // Pre-size matrix and solutions array with 0s
        for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
            ArrayList<Double> row = VirtualCondition.getEmptyRow(nodeCount);
            matrix.add(row);
            solutions.add(0.0);
        }

        // Generate componentTypeMap
        for (AbstractVirtualComponent comp : components) {
        // for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
        //    for (AbstractVirtualComponent comp : nodeMap.get(nodeId)) {
                componentTypeMap.computeIfAbsent(comp.getCondition().type, k -> new ArrayList<>());
                componentTypeMap.get(comp.getCondition().type).add(comp);
        //    }
        }

        // Compute all conditions. Due to order of matrix operations, they must be performed in this order.

        // TODO: replace with this instead of ocmponents
        // VirtualCondition.KCLCondition(componentTypeMap.get(VirtualCondition.Condition.RESISTANCE), matrix, solutions);
        VirtualCondition.KCLCondition(components, matrix, solutions, steadyState);
        VirtualCondition.currentSourceCondition(componentTypeMap.get(VirtualCondition.Condition.CURRENT), matrix, solutions, steadyState);
        VirtualCondition.voltageDifferenceCondition(componentTypeMap.get(VirtualCondition.Condition.VOLTAGE_DIFFERENCE), matrix, solutions, steadyState);
        VirtualCondition.fixedNodeCondition(componentTypeMap.get(VirtualCondition.Condition.FIXED_VOLTAGE), matrix, solutions);

        // Solve matrix equation Ax = b
        SimpleMatrix A = new SimpleMatrix(matrix.size(), nodeCount);
        SimpleMatrix b = new SimpleMatrix(matrix.size(),1);
        ArrayList<Double> nodalVoltages = new ArrayList<>();

        for (int i = 0; i < matrix.size(); i++) {
            A.setRow(i, 0, matrix.get(i).stream().mapToDouble(d -> d).toArray());
            b.set(i, 0, solutions.get(i));
        }

        try {
            SimpleMatrix solution = A.solve(b);
            for (int i = 0; i < solution.getNumElements(); i++)
                nodalVoltages.add(solution.get(i));
        }
        catch(SingularMatrixException e) {
            throw new SingularMatrixException(
                "Circuit solving failure: Matrix cannot be solved\n" +
                "Singular matrix attempting to solve Ax = b\n\n" +
                "Value of A: \n\n" + A.toString() + "\n" +
                "Value of b: \n\n" + b.toString() + "\n" +
                (!containsGround ? "Note: Circuit does not have a ground!\n" : "") +
                "Note: steadyState: " + (steadyState ? "true" : "false")
            );
        }

        return nodalVoltages;
    }

    public void tick() {
        for (AbstractVirtualComponent comp : requireTickComponents)
            comp.tick();
    }


    // --- State getters --- \\

    public double getNodalVoltage(int nodeId) {
        return nodalVoltages.get(nodeId);
    }

    public double getCurrentThrough(int node1, int node2) {
        ArrayList<AbstractVirtualComponent> resistors = null;

        int[] nodes = { node1, node2 };
        int rCount;

        for (int nodeId : nodes) {
            if (nodeMap.get(nodeId).size() == 2 || nodeMap.get(nodeId).size() == 3)
                resistors = nodeMap.get(nodeId);
            if (resistors == null)
                continue;

            rCount = 0;
            for (AbstractVirtualComponent comp : resistors) {
                if (comp instanceof VirtualResistor)
                    rCount++;
            }
            if (rCount != 1) {
                resistors = null;
                continue;
            }
            break;
        }

        if (resistors == null)
            return 0.0;

        for (AbstractVirtualComponent comp : resistors) {
            if (uniqueNodes.size() > 2) {  // TODO doesnt work, fails if only 2 elements are in series and parallel
                // TODO: describe series check
                if (comp.getCondition().node1 == node1 && comp.getCondition().node2 == node2)
                    continue;
                if (comp.getCondition().node2 == node1 && comp.getCondition().node1 == node2)
                    continue;
            }
            if (comp.getCondition().type == VirtualCondition.Condition.RESISTANCE) {
                if (comp.getCondition().node1 == node1 || comp.getCondition().node2 == node2)
                    return -comp.getCurrent();
                return comp.getCurrent();
            }
        }
        return 0.0;
    }
}
