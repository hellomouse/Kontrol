package net.hellomouse.kontrol.logic.circuit;

/**
 * IDs of particles (Only directly on the skeleton)
 * ids      - Vector of ids of conductors
 * rspk_ids - Vector of ids of RSPK
 * switches - Vector of ids of switches
 *
 * total_resistance - Equivalent resistance of the branch (includes end node, but not start)
 * total_voltage    - Equivalent voltage source of the branch, this overrides resistance
 * current_voltage  - Used for polarity measurements
 *
 * diode_type     - 0 = branch is not a diode, 1 = positive diode, 2 = negative diode
 * polarity       - Track polarity of objects such as VOLT and diodes, 0, 1 = positive, -1 = negative
 */

public class CircuitNode {
}
