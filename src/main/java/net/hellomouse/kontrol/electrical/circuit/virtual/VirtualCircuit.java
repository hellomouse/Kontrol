package net.hellomouse.kontrol.electrical.circuit.virtual;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.*;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.ICurrentCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IFixedVoltageCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IResistanceCondition;
import org.apache.logging.log4j.LogManager;
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
    // Does circuit contain a ground (or any fixed voltage point)
    private boolean containsFixedVoltagePoint = false;
    // How many ways can the circuit be supplied with energy?
    private int energySourceCount = 0;
    // Has circuit been solved at least once?
    private boolean solved = false;

    // Optimization settings
    private VirtualCircuitSettings settings = new VirtualCircuitSettings();
    // Number of times circuit has been ticked
    private int ticks = 0;

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

        for (VirtualCondition.Condition key : VirtualCondition.Condition.values()) {
            if (VirtualCondition.fitsKey(component, key)) {
                conditionComponentMap.computeIfAbsent(key, k -> new ArrayList<>());
                conditionComponentMap.get(key).add(component);
            }
        }

        if (node1 != node2) {
            uniqueNodes.add(node2);
            nodeMap.computeIfAbsent(node2, k -> new ArrayList<>());
            nodeMap.get(node2).add(component);
        }

        // Checks for circuit
        if (component instanceof IFixedVoltageCondition)
            containsFixedVoltagePoint = true;
        if (component.requireTicking())
            requireTickComponents.add(component);

        component.initialUpdateEnergySourceCount();

        // Add special components
        if (component.isNonLinear())
            nonLinearComponents.add(component);
    }

    /**
     * Solves the circuit given the components.
     *
     * Caveats:
     * - All nodes ID must be used up to the max. ie, if the highest nodeId is 3, then
     *   nodes 0, 1, 2, and 3 must all exist
     */
    public void solve() {
        if (!containsFixedVoltagePoint && energySourceCount > 0) { // No ground, randomly assign a voltage source's node to ground
            // Randomly select a valid voltage or current source
            ArrayList<AbstractVirtualComponent> comps = conditionComponentMap.get(VirtualCondition.Condition.VOLTAGE_DIFFERENCE);
            if (comps == null || comps.size() == 0)
                comps = conditionComponentMap.get(VirtualCondition.Condition.CURRENT);
            if (comps != null && comps.size() > 0)
                addComponent(new VirtualGround(), comps.get(0).getNode2(), comps.get(0).getNode2());
        }

        // Reset diodes to OFF by default
        if (ticks % settings.resetDiodesEveryNTicks == 0) {
            for (AbstractVirtualComponent comp : nonLinearComponents)
                comp.setHiZ(true);
        }

        nodalVoltages = solveHelper(false);
        recomputeSpecialCases();
    }

    /**
     * Solves non-linear components (ie, diodes), and resolves circuit
     * if necessary. Only re-solves circuit once, any errors will be corrected
     * in future resolves, which for most (hopefully) circuits is a short amount of time.
     */
    private void recomputeSpecialCases() {
        for (int i = 1; i < settings.maxIterations; i++) {
            boolean recompute = false;

            // Diode computations
            for (AbstractVirtualComponent comp : nonLinearComponents) {
                if (comp instanceof VirtualDiode) {
                    // Enable diode if forward voltage reached
                    double V = comp.getVoltage();
                    double I = comp.getCurrent();
                    boolean oldState = comp.isHiZ();
                    boolean newState = !(-V >= ((VirtualDiode) comp).getVForward() && I > 0);

                    if (oldState != newState) {
                        recompute = true; // Always recompute diodes
                        steadyStateNodalVoltages.clear(); // Diodes may alter steady state voltages, clear cache
                        comp.setHiZ(newState);
                    }
                }
            }

            if (!recompute) break; // No need to further iterate, reached steady state
            nodalVoltages = solveHelper(false);
        }
    }

    /**
     * Matrix solver for current circuit state
     * @param steadyState Should it solve for steady state condition?
     * @return Nodal voltage ArrayList
     */
    private ArrayList<Double> solveHelper(boolean steadyState) {
        int nodeCount = uniqueNodes.size();
        solved = true;

        // 1 node circuit, or no energy source circuits have all nodes = 0 V
        if (nodeCount < 2 || energySourceCount == 0)
            return getEmptyRow(nodeCount);

        SimpleMatrix matrix    = new SimpleMatrix(nodeCount, nodeCount);
        SimpleMatrix solutions = new SimpleMatrix(nodeCount, 1);

        // Compute all conditions. Due to order of matrix operations, they must be performed in this order.
        // See VirtualCondition for explanations of what each solver does
        VirtualCondition.KCLCondition(components, matrix, solutions, steadyState);
        VirtualCondition.currentSourceCondition(conditionComponentMap.get(VirtualCondition.Condition.CURRENT), solutions, steadyState);
        VirtualCondition.voltageDifferenceCondition(conditionComponentMap.get(VirtualCondition.Condition.VOLTAGE_DIFFERENCE), matrix, solutions, steadyState);
        VirtualCondition.fixedNodeCondition(conditionComponentMap.get(VirtualCondition.Condition.FIXED_VOLTAGE), matrix, solutions);
        // VirtualCondition.customCondition(conditionComponentMap.get(VirtualCondition.Condition.CUSTOM), matrix, solutions);

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
                (!containsFixedVoltagePoint ? "Note: Circuit does not have a ground, one was auto-added\n" : "") +
                "Note: steadyState: " + (steadyState ? "true" : "false")
            );
        }

        return nodalVoltages;
    }

    /**
     * Does numeric integration and other stuff components need to do
     * every tick (component.tick() for every component)
     */
    public void tick() {
        ticks++;
        for (AbstractVirtualComponent comp : requireTickComponents)
            comp.tick();
    }

    /**
     * Returns current from node1 to node2, current from node1 to node2
     * is considered positive, node2 to node1 is negative. Current is computed
     * from series resistors, so this will fail if there is no resistor(s) in series
     * for a component from node1 to node2.
     *
     * In other words, there must be only 1 component from node1 to node2, or an incorrect
     * result may be returned.
     *
     * @param node1 Node id
     * @param node2 Node id
     * @return Current
     */
    public double getCurrentThrough(int node1, int node2) {
        ArrayList<AbstractVirtualComponent> seriesComponents = null;
        int resistorCount;
        double totalCurrent = 0.0;
        int[] nodes = { node1, node2 };

        for (int nodeId : nodes) {
            resistorCount = 0;
            totalCurrent = 0.0;
            seriesComponents = nodeMap.get(nodeId);

            if (seriesComponents == null)
                continue;

            // Components that are not resistors but are ignored for current
            // calculation purposes
            int specialComponentCount = 0;

            for (AbstractVirtualComponent comp : seriesComponents) {
                boolean isResistor = comp instanceof IResistanceCondition;
                boolean isCurrentSource = comp instanceof ICurrentCondition;

                if (comp instanceof IFixedVoltageCondition)
                    specialComponentCount++;
                if (isResistor)
                    resistorCount++;
                if (isCurrentSource) {
                    specialComponentCount++;
                    totalCurrent += comp.getCurrent();
                }

                // Component found exactly matching nodes and it has a pre-determined
                // current (Resistors and current sources know their currents)
                // So we don't need to sum series resistors, juts return the component directly

                // The uniqueNodes.size() > 2 check is because for a 2 node circuit anything
                // in series will have the same nodes

                if (uniqueNodes.size() > 2 && (isResistor || isCurrentSource)) {
                    if (comp.getNode1() == node1 && comp.getNode2() == node2)
                        return comp.getCurrent();
                    if (comp.getNode2() == node1 && comp.getNode1() == node2)
                        return -comp.getCurrent();
                }
            }

            // Verify all components, except the component we're measuring across (assumed = 1) and the
            // exempt components (specialComponentCount) are resistors
            if ((resistorCount != seriesComponents.size() - 1 - specialComponentCount) || resistorCount == 0)
                continue;
            break;
        }

        // No series resistors were found
        if (seriesComponents == null) {
            LogManager.getLogger().warn("No resistors were found in series for nodes " + node1 + " to " + node2);
            return 0.0;
        }

        for (AbstractVirtualComponent comp : seriesComponents) {
            // Skip the component we're measuring across
            // See the if (uniqueNodes.size() > 2) above
            if (uniqueNodes.size() > 2) {
                if (
                        (comp.getNode1() == node1 && comp.getNode2() == node2) ||
                        (comp.getNode2() == node1 && comp.getNode1() == node2))
                    continue;
            }

            // If resistor starts at node1 or ends at node2, then current is going wrong way
            // [R1_node2] <---- [R1_node1 = node1] ----> [node2 = R2_node2] <----- [R2_node1]
            // Note the middle arrow (direction we're measuring) is opposite of the resistors

            if (comp instanceof IResistanceCondition) {
                if (comp.getNode1() == node1 || comp.getNode2() == node2)
                    totalCurrent += -comp.getCurrent();
                else
                    totalCurrent += comp.getCurrent();
            }
        }
        return totalCurrent;
    }

    /**
     * Clears all storage arrays, effectively
     * resetting the circuit instance.
     */
    public void clear() {
        components.clear();
        nodeMap.clear();
        conditionComponentMap.clear();
        nonLinearComponents.clear();
        requireTickComponents.clear();
        uniqueNodes.clear();
        nodalVoltages.clear();
        steadyStateNodalVoltages.clear();
        containsFixedVoltagePoint = false;
        energySourceCount = 0;
    }

    /**
     * Returns an arraylist of size filled with all 0s
     * @param size Size of arraylist
     * @return Arraylist
     */
    public static ArrayList<Double> getEmptyRow(int size) {
        ArrayList<Double> row = new ArrayList<>(Arrays.asList(new Double[size]));
        Collections.fill(row, 0.0);
        return row;
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

    /**
     * Return nodal voltages at steady state -- computes steady state
     * if it's not cached, which requires a resolve.
     * @return Steady state
     */
    public ArrayList<Double> getSteadyStateNodalVoltages() {
        if (steadyStateNodalVoltages.size() == 0)
            steadyStateNodalVoltages = solveHelper(true);
        return steadyStateNodalVoltages;
    }

    /**
     * Get voltage at node id
     * @param nodeId node id
     * @return Voltage
     */
    public double getNodalVoltage(int nodeId) {
        return nodalVoltages.get(nodeId);
    }

    /** Add or subtract energy source count */
    public void incEnergySources() { energySourceCount++; }
    public void decEnergySources() { energySourceCount--; }
    public int getEnergySourceCount() { return energySourceCount; }

    /**
     * Has circuit been solved at least once?
     * @return Solved state
     */
    public boolean isSolved() { return solved; }

    /**
     * Set the circuit settings to settings
     * @param settings New settings
     */
    public void changeSettings(VirtualCircuitSettings settings) {
        this.settings = settings;
    }


    /**
     * Optimization settings for the circuit
     * @author Bowserinator
     */
    public static class VirtualCircuitSettings {
        private final int maxIterations; // Default only solve() twice
        private final int resetDiodesEveryNTicks ; // Reset diode states to Hi-Z for proper solving every 2 iterations

        /** Construct settings with default values */
        public VirtualCircuitSettings() {
            this(2, 2);
        }

        /**
         * Construct settings
         * @param maxIterations Max iterations to try to solve non-linear components per solve()
         *                      High numbers may result in lots of circuit re-solves which can impact performance
         *                      Low numbers may cause circuits with lots of non-linear components to converge very slowly
         * @param resetDiodesEveryNTicks Every n ticks diodes will be reset to Hi-Z to re-solve properly
         *                               Setting this too low will result in unnecessary re-solves, while setting it too high
         *                               will result in improper diode behavior for fast changing circuits
         */
        public VirtualCircuitSettings(int maxIterations, int resetDiodesEveryNTicks) {
            this.maxIterations = maxIterations;
            this.resetDiodesEveryNTicks = resetDiodesEveryNTicks;
        }
    }
}
