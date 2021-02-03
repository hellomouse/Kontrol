package net.hellomouse.kontrol.electrical.circuit.virtual;

/**
 * Circuit constants
 * @author Bowserinator
 */
public class VirtualCircuitConstants {
    // Time in seconds attributed to 1 tick
    // Effectively an integration time step
    public static final double DT = 0.05;

    public static final double UNKNOWN_ENERGY = -1.0;

    // Used for modelling initial resistance condition for capacitors / inductors
    public static final double CAPACITOR_INITIAL_R = 1e-3;
    public static final double INDUCTOR_INITIAL_R = 1e9;


    // Used mainly in modelling steady state and disabled components
    public static final double OPEN_CIRCUIT_R = 1e9;
    public static final double SHORT_CIRCUIT_R = 1e-9;
}
