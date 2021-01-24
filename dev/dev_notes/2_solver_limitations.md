# Limitations and Bugs

The simple circuit solver written in part 1 has some issues...



### Limitations

- **Double precision:** Imagine we have a resistor of resistance `1e-99` and another with resistance `1` connected. When we do KCL we have an equation like `1e99 - 1`. Due to Java's double precision, we cannot actually store the result; thus the resulting  answer is actually still `1e99`. This can turn a solvable matrix into an unsolvable one. 
- **Floating nodes:** When an element is disabled, we turn it into a high value resistor to avoid dealing with undefined floating nodes.
- **Must have ground node:** Pretty self explanatory, or else some circuits are unsolvable.



### Resistors & Elements

 Non-resistive elements must be wrapped by resistors (be connected to resistors on both sides in series) and cannot directly connect other non-resistive elements. Current sources are similar in that they must have a high value resistor in parallel.

The reason is two fold:

- It allows us to calculate the current through any element easily using ohm's law

- The matrix techniques we used make this assumption, and doing otherwise can make an invalid matrix

    

There are also several bugs that can occur when this assumption doesn't hold:

- If a voltage source is directly connected to ground, the fixed node equation can overwrite the supernode equation for the voltage source (which would of course, make the equation unsolvable)

- If two current sources are joined in series without the parallel resistor, "impossible" states can occur (for example, a 1 A current source in series with a 2 A). Even if both current sources are the same value the resulting matrix may still be unsolvable.

    

Finally, when we invalidate elements, we must set them as a high value resistor (to make them open) rather than remove them from the circuit. This prevents:

- Having to re-construct the matrix as nodal connections have changed
- Having floating nodes, ie imagine A, B, C are in series. If A and C are disabled, then B has two nodes going to nowhere, which is an unsolvable condition.



### Current Bugs

The float point precision can make normally solvable equations either unsolvable or give out completely wrong answers. For example, an RL circuit with a very low R relative to the very high parallel resistance for each inductor will make an unsolvable matrix. Interestingly, the matrix solver can still give a result (albeit a wrong one) in some instances.

Perhaps the solver can be refactored in the future to use higher precision numbers, or a lib like JScience.