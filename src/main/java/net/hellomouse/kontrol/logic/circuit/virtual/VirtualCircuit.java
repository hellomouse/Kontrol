package net.hellomouse.kontrol.logic.circuit.virtual;

import net.hellomouse.kontrol.logic.circuit.virtual.components.*;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * A virtual circuit that can add virtual components,
 * tick and be solved. Virtual as in this contains no references
 * to the Minecraft world or game state
 * @author Bowserinator
 */
public class VirtualCircuit {
    // All components
    private final ArrayList<AbstractVirtualComponent> components = new ArrayList<>();
    // Condition to component map
    private final Map<VirtualCondition.Condition, ArrayList<AbstractVirtualComponent>> conditionComponentMap = new HashMap<>();
    // NodeID: All components that connect to that node
    private final Map<Integer, ArrayList<AbstractVirtualComponent>> nodeMap = new HashMap<>();
    // Components that use numeric integration
    private final ArrayList<AbstractVirtualComponent> divergentComponents = new ArrayList<>();
    // Components that are non-linear, such as diodes
    private final ArrayList<AbstractVirtualComponent> nonLinearComponents = new ArrayList<>();
    // Components that need to tick() when this.tick() is called
    private final ArrayList<AbstractVirtualComponent> requireTickComponents = new ArrayList<>();

    // Set of unique nodeIDs
    private final Set<Integer> uniqueNodes = new TreeSet<>();
    // Solutions for voltages at every node
    private ArrayList<Double> nodalVoltages = new ArrayList<>();
    // Solutions for voltages at every node at steady state
    private ArrayList<Double> steadyStateNodalVoltages = new ArrayList<>();
    // Does circuit contain a ground?
    private boolean containsGround = false;
    // Does the circuit have any way to be supplied with energy?
    private boolean containsEnergySource = false;

    /**
     * Add a component from node1 to node2. See Polarity tests
     * for polarities for each component
     * @param component Component.
     * @param node1 Positive node
     * @param node2 Negative node
     */
    public void addComponent(AbstractVirtualComponent component, int node1, int node2) {
        uniqueNodes.add(node1);

        component.setNodes(node1, node2);
        component.setCircuit(this);
        components.add(component);

        nodeMap.computeIfAbsent(node1, k -> new ArrayList<>());
        nodeMap.get(node1).add(component);

        conditionComponentMap.computeIfAbsent(component.getCondition().type, k -> new ArrayList<>());
        conditionComponentMap.get(component.getCondition().type).add(component);

        if (node1 != node2) {
            uniqueNodes.add(node2);
            nodeMap.computeIfAbsent(node2, k -> new ArrayList<>());
            nodeMap.get(node2).add(component);
        }

        // Checks for circuit
        if (component instanceof VirtualGround)
            containsGround = true;
        if (component.requireTicking())
            requireTickComponents.add(component);

        // Check circuit contains an energy source, otherwise all voltages
        // are zero by default
        VirtualCondition condition = component.getCondition();
        if (condition.value != 0.0 && (
                condition.type == VirtualCondition.Condition.VOLTAGE_DIFFERENCE  ||
                condition.type == VirtualCondition.Condition.CURRENT ||
                condition.type == VirtualCondition.Condition.FIXED_VOLTAGE))
            containsEnergySource = true;

        // Add special components
        if (component.doesNumericIntegration())
            divergentComponents.add(component);
        if (component.isNonLinear())
            nonLinearComponents.add(component);
    }

    /**
     * Solves the circuit given the components.
     *
     * Caveats:
     * - All nodes ID must be used up to the max. ie, if the highest nodeId is 3, then
     *   nodes 0, 1, 2, and 3 must all exist
     * - Non-resistors must only connect to resistors (Ie, no directly chaining voltage sources)
     */
    public void solve() {
        if (!containsGround) { // No ground, randomly assign a voltage source's node to ground
            ArrayList<AbstractVirtualComponent> voltageComps = conditionComponentMap.get(VirtualCondition.Condition.VOLTAGE_DIFFERENCE);
            if (voltageComps != null && voltageComps.size() > 0)
                addComponent(new VirtualGround(), voltageComps.get(0).getCondition().node2, voltageComps.get(0).getCondition().node2);
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
                    // - We do not reach V_breakdown
                    double V = comp.getVoltage();
                    double I = comp.getCurrent();

                    boolean oldState = comp.isDisabled();
                    boolean newState = !(I < 0 && Math.abs(V) >= ((VirtualDiode)comp).getVForward());

                    if (oldState != newState) {
                        recompute = true; // Always recompute diodes
                        steadyStateNodalVoltages.clear(); // Diodes may alter steady state voltages, clear cache
                        comp.setDisabled(newState);
                    }
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
        if (nodeCount < 2 || !containsEnergySource)
            return VirtualCondition.getEmptyRow(nodeCount);

        // Helper data for generating final matrix
        SimpleMatrix matrix    = new SimpleMatrix(nodeCount, nodeCount);
        SimpleMatrix solutions = new SimpleMatrix(nodeCount, 1);


        // Compute all conditions. Due to order of matrix operations, they must be performed in this order.
        VirtualCondition.KCLCondition(components, matrix, solutions, steadyState);
        VirtualCondition.currentSourceCondition(conditionComponentMap.get(VirtualCondition.Condition.CURRENT), matrix, solutions, steadyState);
        VirtualCondition.voltageDifferenceCondition(conditionComponentMap.get(VirtualCondition.Condition.VOLTAGE_DIFFERENCE), matrix, solutions, steadyState);
        VirtualCondition.fixedNodeCondition(conditionComponentMap.get(VirtualCondition.Condition.FIXED_VOLTAGE), matrix, solutions);

        // Solve matrix equation Ax = b
        ArrayList<Double> nodalVoltages = new ArrayList<>(nodeCount);

        try {
            SimpleMatrix solution = matrix.solve(solutions);
            for (int i = 0; i < solution.getNumElements(); i++)
                nodalVoltages.add(solution.get(i));
        }
        catch(SingularMatrixException e) {
            throw new SingularMatrixException(
                "Circuit solving failure: Matrix cannot be solved\n" +
                "Singular matrix attempting to solve Ax = b\n\n" +
                "Value of A: \n\n" + matrix.toString() + "\n" +
                "Value of b: \n\n" + solutions.toString() + "\n" +
                (!containsGround ? "Note: Circuit does not have a ground, one was auto-added\n" : "") +
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

    /**
     * Clears all storage arrays, effectively
     * resetting the circuit instance.
     */
    public void clear() {
        components.clear();
        nodeMap.clear();
        conditionComponentMap.clear();
        divergentComponents.clear();
        nonLinearComponents.clear();
        requireTickComponents.clear();
        uniqueNodes.clear();
        nodalVoltages.clear();
        steadyStateNodalVoltages.clear();
        containsGround = false;
        containsEnergySource = false;
    }

    /**
     * Returns highest node ID used. Assumes nodes
     * are numbered sequentially from 0, ie 0, 1, 2, ...
     * @return Highest node id
     */
    public int getHighestNodeID() {
        return uniqueNodes.size() - 1;
    }

    /**
     * Return ArrayList of all components
     * @return Components
     */
    public ArrayList<AbstractVirtualComponent> getComponents() {
        return components;
    }
}
