# Note on Condition Interfaces

A hack for mutually excluding certain interfaces: all interfaces have a default method called `_preventDuplicate()` with a different
return type for exclusive interfaces.

Below are the groups for each condition:

---

## Resistance and Current Source

These two each affect different sides of the matrix (resistance alters main matrix, current source alters solution matrix) so they
do not conflict and using the two conditions together is a simple way to model a resistor in parallel with a current source.

Return type: float

```java
default float _preventDuplicate() { return 0.0f; }
```

## Voltage Difference

Defines two nodes to have a fixed voltage difference between them.

Return type: double

```java
default double _preventDuplicate() { return 0.0; }
```

## Fixed Voltage

A node is constrained to a specific voltage. Mainly used for ground nodes.

Return type: int

```java
default int _preventDuplicate() { return 0; }
```

## Custom

Should be used for any special condition that doesn't satisfy a combination of the above. A custom component will have to implement

```java
modifyMatrix(SimpleMatrix matrix, SimpleMatrix solutions)
```

where it can modify the matrices directly.

Return type: char

```java
default char _preventDuplicate() { return ' '; }
```