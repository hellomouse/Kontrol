# Component Behaviors

This page contains documentation for various behaviors of electrical components. As VirtualCircuit only contains basic linear circuit components that more complex components can be built out of, it was not designed to be more flexible than necessary. This document outlines where certain behaviors in the code are done for reference, mainly for modifying the behavior of an existing component. For example, if you wish to:

- Rewrite voltage sources to be modelled after a current source
- Change the internal model of a capacitor or inductor
- Add a custom component (not recommended)



## Disabled Components

Components can be disabled with `component.setDisabled(true)`. This behavior varies between components:

- **Voltage sources:** Voltage difference is set to 0
- **Current sources:** Current value is set to 0
- **Fixed nodes:** Nodal equation is ignored (Note: this may cause invalid matrices!)
- **Custom:** Up to `component.modifyMatrix()`, no explicit handling of disabled case. The code for solving custom conditions is commented out by default.
- **Other:** Disabled state is ignored



## Hi-Z

Components can be set to high-impedance with `component.setHiZ(true)`. When enabled, all components will act as a high value resistor instead of their normal behavior.

The resistance setting code can be found in `VirtualCondition.KCLCondition`.



## Numeric Integration

### Steady state

Steady state impedance is hard coded in `VirtualCondition.KCLCondition` for inductors and capacitors; if you add a new component that does numeric integration you will need to specify a steady state impedance here if it's different from the capacitor's.

Additional steady state ignores may be added in `VirtualCondition.currentSourceCondition` and `VirtualCondition.voltageDifferenceCondition`, hardcoded for inductors and capacitors depending on how they are implemented.

Steady state divergence checking is done in `VirtualCircuit.recomputeSpecialCases`, and depends on methods that are defined in the `INumericIntegration` interface.



## Energy Sources

Any component that can make energy (voltage / current sources, fixed voltage nodes) must do the following:

- Whenever the current or voltage value changes, increment or decrement the number of energy sources in the circuit accordingly. An example from `VirtualVoltageSource` is shown below:

    ```java
    public void setVoltage(double voltage) {
        if (this.voltage == 0.0 && voltage != 0.0)
            circuit.incEnergySources();
        else if (this.voltage != 0.0 && voltage == 0.0)
            circuit.decEnergySources();
        this.voltage = voltage;
    }
    ```

- What constitutes as an energy source is hardcoded in `VirtualCircuit.addComponent`.