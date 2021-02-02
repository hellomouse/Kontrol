package net.hellomouse.kontrol.logic.circuit.virtual;

/**
 * Circuit constants
 * @author Bowserinator
 */
public class VirtualCircuitConstants {
    // Time in seconds attributed to 1 tick
    // Effectively an integration time step
    public static final double DT = 0.05;

    public static final double UNKNOWN_ENERGY = -1.0;

    // Used mainly in modelling steady state and disabled components
    public static final double OPEN_CIRCUIT_R = 1e9;
    public static final double CLOSED_CIRCUIT_R = 1e-9;
}
