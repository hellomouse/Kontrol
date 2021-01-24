# Floodfill

To generate the circuit, we need to perform a floodfill on all connected components, then generate a circuit map of it.

Since circuits are usually stretches of straight lines, a queue based implementation should be fine, with no need for scanlines. Of course, a malicious user could purposefully build a 100x100x100 cube of wires and constantly update it, but you can already make lag machines with vanilla game mechanics, and in general modded servers are made of small groups of trusted people.



## Version 1

The first attempt looked something like this. This was just an unoptimized version to see if circuits updated properly when blocks changed.

```java
Queue<BlockPos> posToVisit = new LinkedList<>();
HashSet<BlockPos> seenPos = new HashSet<>();
posToVisit.add(pos);

while (posToVisit.size() > 0) {
    BlockPos p = posToVisit.remove();
    seenPos.add(p);

    ((AbstractElectricalBlockEntity)world.getBlockEntity(p)).setCircuit(this);

    for (Direction dir : Direction.values()) {
        BlockPos newPos = p.offset(dir);
        BlockEntity newEntity = world.getBlockEntity(newPos);

        if (newEntity instanceof AbstractElectricalBlockEntity && !seenPos.contains(newPos))
            posToVisit.add(newPos);
    }
}
```

It runs extremely slow for a 8x8x8 cube (debug screen showed it used up all the memory), and about 50 ms for a 4x4x4 cube (only a 64 large circuit), which is unacceptable for gameplay. For reference, a tick should take *at most* 50 ms before the server can no longer process events in real time.



### Version 2

An enormous number of unnecessary memory allocations and searches can be removed by getting rid of the HashSet that tracks visited locations, and instead checking if the BlockEntity's circuit at the location was already set:

**Before:**

```java
if (newEntity instanceof AbstractElectricalBlockEntity && !seenPos.contains(newPos))
```

**After:**

```java
if (newEntity instanceof AbstractElectricalBlockEntity && ((AbstractElectricalBlockEntity)newEntity).getCircuit() != this)
```

For an 8x8x8 cube of wires, a floodfill takes 1-2 ms. Much better!



## Version 3

Optimizing at this point is focused on avoid checking a spot more than once. In 3D, if we check all 6 sides of a cube for every spot, every spot will be checked 5 times more than necessary. We can reduce this by restricting floodfill repeatedly to a lower dimension.

Suppose we started a floodfill, but restricted it to filling in the WEST and EAST directions. We check UP, DOWN, NORTH and SOUTH for new spots to spread to and add those as possible starting locations to check the WEST and EAST axis from.

[TODO diagram]

We can perform a floodfill on that plane, and check every point on there for a west-east analog.

[TODO figure it out??]