# Solving Circuits (Abstractly)

In order to make our circuit blocks, we separate the circuit into a *real* portion (the in-game world) and an *abstract* potion (a mathematical representation of the circuit). This article focuses on solving the circuit in the abstract portion, which can be later translated between the real portion.



## Solving (Theory)

This section is a rundown of basic techniques you would use to solve the circuit, as you may on a homework problem.

The majority of the circuit is solved by using KCL. Let's call the voltage at each circuit node $$V_0, V_1, ... V_n$$. For each node we can write an equation that relates all the nodal voltages, creating a system of linear equations. We use the Java library EJML to solve the matrix (solving the linear system).



### Fixed Nodes

The equation for a fixed node is simple, $$V_i = V_{fixed}$$ , where node *i* is fixed to a specific voltage. We use this in ground nodes (where voltage is fixed to 0).



### Resistive elements

By KCL, the net current into a node on a circuit is zero (ignoring the existence of a current source). For example, the equation for node one in the following diagram is:

![Ohms law diagram](https://i.imgur.com/eYkLUFp.png)

[TODO: hand draw the diagrams instead??]

The current for each resistor is determined via ohm's law (difference in voltage divided by resistance).



### Voltage sources

We use the concept of a supernode to compute voltage sources. This step is carried out after all KCL equations are written. For example, consider the layout below:

[TODO DIAGRAM]

We note that $I_{N1} + I_{N2} = 0$ (as opposed to an ordinary node, where the current into any node is zero, as in $I_{N1} = I_{N2} = 0$) when we treat $N1$ and $N2$ as a single node. We also node a relation between the two nodal voltages: $V_{N2} - V_{N1} = V_{Source}$. 

Using this supernode we can replace the KCL equations for the two nodes with the two equations above.



### Current Sources

This step is carried out after all KCL equations are written. For example, consider the layout below:

[TODO DIAGRAM]

If we ignored the current source when doing KCL, the net current out (or in, depending on your polarity) of node one would be non-zero. We would have to subtract the current from the current source to get it to zero, which is the same as setting the sum of the currents of the resistors to a non-zero value, as in, $I_{R1} + I_{R2} + I_{R3} = -I_{Source}$



### Capacitors

Capacitors are "dynamic voltage sources" that obey the equation $i = C \frac{dV}{dt}$, or in other words, the voltage across the capacitor at a moment in time is the integral of the current over the capacitance.

Thus we can use numeric integration to solve for the voltage across the capacitor at any moment. In our case for speed we use Euler's method, where $V_{t + dt} = V_{t} + dt \cdot I_{t} / C$.



### Inductors

Inductors are "dynamic current sources" that obey the equation $V = L \frac{di}{dt}$, or in other words, the current across the inductor at a moment in time is the integral of the voltage over the inductance.

Like the capacitor, we use Euler's method, where $I_{t + dt} = I_{t} + dt \cdot V_{t} / L$.



### Diodes

To avoid the computational overhead of iteratively solving diode equations, we use very ideal diodes. The I-V curve looks something like this:

[TODO DIAGRAM]

After an initial solve of the circuit, the voltage across each diode is considered. If the voltage is in the right direction and does not meet $V_{forward}$, then the diode is modelled as a very high resistance resistor. Otherwise, the diode functions as a $-V_{forward}$ voltage source. The matrix is re-computed once again (not recomputed again after this).



### Switches, potentiometers, etc...

These components are not explicitly modeled in the code, but can easily be modelled as an resistor with a variable resistance, for example, a switch toggles between 0 and infinite (in the code, a really large, but finite) resistance.



## Additional notes

### Polarities used

### Numeric integration instability

Components that do numeric integration such as capacitors and inductors may become unstable when the time constant is much smaller than the timestep. 

For instance, imagine a RC circuit with `V = 10 V`, `R = 1 ohm` and `C = 1e-6 F`. Ignoring any realistic effects, this circuit would have a time constant of approximately 1 microsecond. Our integration timestep is 1 Minecraft tick (1 / 20th of a second). If we were to plot the voltage across the capacitor over every step, here's what we would get:

[TODO diagram]

Instead of stopping at 10 V like we expected, the capacitor overshoots it enough to reach a positive feedback loop, causing the voltage to increase to infinity!



**How we deal with instability**

We could:

- A smaller timestep would reduce the range the instability occurs
- A better integration technique like backwards Euler or a higher order Runge-Kutta may be more stable

While these methods would drastically reduce the ranges that cause instability, this would not solve the instability in all cases, or would be computationally expensive (for a Minecraft circuit mod), as we would need to evaluate the circuit in a future state to estimate the future value of the derivative.

Luckily, we know how capacitors and inductors behave at steady state (capacitors are open, inductors short), so we can re-evaluate the circuit using these assumptions (Note: we treat open as having a large resistance to avoid dealing with nasty floating nodes that make the equation unsolvable).

Assuming the component will never* overshoot steady state, if the component does go over, we simply snap it to its steady state value.



***Caveat**

Unfortunately this breaks certain RLC circuits, which can overshoot, for example, if it's underdamped. We'll address this problem later.


