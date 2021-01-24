# Furnace Generator

Time to add some somewhat complicated blocks. Any circuit needs a voltage source, so let's add an early game source of electricity, a burnable-fuel-powered generator.



## Models, Textures

![Models](https://i.imgur.com/tgRTSIx.png)

*Some early textures and models made in BlockBench*

A simple furnace like texture. The red and blue ports (blue port not visible) will connect the wires. The front flickers a bit when on (animated textures), and the top glows a dull red, indicating it's hot. The ports also light up when the furnace is on, indicating they're powered.

BlockBench automatically exports the custom model JSON, but as it's not based on Minecraft's default block the item model it has the flat item properties set by default, so we have to set it on our own. Luckily it wasn't too hard to find what Minecraft uses for its default block item models (`assets/kontrol/models/item/furnace_generator.json`:

```json
{
	"parent": "kontrol:block/furnace_generator",
	"display": {
		"gui": {
			"rotation": [ 30, 45, 0 ],
			"translation": [ 0, 0, 0],
			"scale":[ 0.625, 0.625, 0.625 ]
		},
		"ground": {
			"rotation": [ 0, 0, 0 ],
			"translation": [ 0, 3, 0],
			"scale":[ 0.25, 0.25, 0.25 ]
		},
		"fixed": {
			"rotation": [ 0, 180, 0 ],
			"translation": [ 0, 0, 0],
			"scale":[ 0.5, 0.5, 0.5 ]
		},
			"thirdperson_righthand": {
			"rotation": [ 75, 225, 0 ],
			"translation": [ 0, 2.5, 0],
			"scale": [ 0.375, 0.375, 0.375 ]
		},
			"firstperson_righthand": {
			"rotation": [ 0, 45, 0 ],
			"translation": [ 0, 0, 0 ],
			"scale": [ 0.40, 0.40, 0.40 ]
		},
			"firstperson_lefthand": {
			"rotation": [ 0, 225, 0 ],
			"translation": [ 0, 0, 0 ],
			"scale": [ 0.40, 0.40, 0.40 ]
		}
	}
}
```

*Reference for default block like item model transformations*



## Blockstates

The furnace generator will have two blockstates: rotation and lit (if it's on). The blockstate json just selects which model and what model rotation to use based on these states (`assets/kontrol/blockstates/furnace_generator.json`):

```json
{
  "variants": {
    "facing=east,lit=false": {
      "model": "kontrol:block/furnace_generator",
      "y": 90
    },
    "facing=east,lit=true": {
      "model": "kontrol:block/furnace_generator_on",
      "y": 90
    },
    ...
```

To register the blockstates, we create a new Boolean state property for LIT and register it.

```java
public class FurnaceGenerator extends ... {
    public static final BooleanProperty LIT = BooleanProperty.of("lit");

    public FurnaceGenerator(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        super.appendProperties(stateManager);
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(LIT);
    }
}
```

We also set the rotation state when we place the block.

```java
@Override
public BlockState getPlacementState(ItemPlacementContext ctx) {
    return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing());
}
```



## Crafting and Gameplay

The crafting recipe is under `resources/data/kontrol/recipes/furnace_generator.json`:

```json
{
    "type": "minecraft:crafting_shaped",
    "pattern": [
        "iii",
        "SFS",
        "SRS"
    ],
    "key": {
        "i": { "item": "minecraft:iron_ingot" },
        "S": { "item": "minecraft:stone" },
        "F": { "item": "minecraft:furnace" },
        "R": { "item": "minecraft:redstone" }
    },
    "result": {
        "item": "kontrol:furnace_generator",
        "count": 1
    }
}
```

The recipe might be changed in the future to use a mod specific block (like a wire or a machine chassis) to avoid possible conflicts with other mods.

---

Under `resources/data/kontrol/loot_tables/blocks/furnace_generator.json` we let the block drop itself when mined (otherwise it drops nothing by default):

```json
{
	"type": "minecraft:block",
	"pools": [{
		"rolls": 1,
		"entries": [{
			"type": "minecraft:item",
			"name": "kontrol:furnace_generator"
		}],
		"conditions": [{
			"condition": "minecraft:survives_explosion"
		}]
	}]
}
```



## Name

To make a proper name display in-game and not something like `kontrol.furnace_generator`, under `resources/assets/kontrol/lang/en_us.json` we add:

```json
"block.kontrol.furnace_generator": "Furnace Generator",
```



## World Interactions

### Top gets hot

When it's on the top looks like it's red hot. It would make sense if you were damaged while standing on it, and that water would make some sizzling noises. Luckily, both behaviors are already present in Magma blocks, so the code below is copied from that:

```java
@Override
public void onSteppedOn(World world, BlockPos pos, Entity entity) {
    // Coal generator is hot when on
    if (world.getBlockState(pos).get(LIT)) {
        if (!entity.isFireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity))
            entity.damage(DamageSource.HOT_FLOOR, 1.0F);
    }
    super.onSteppedOn(world, pos, entity);
}

@Override
public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    BlockPos blockPos = pos.up();
    if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
        world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.25D, (double)blockPos.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
    }
}
```


### Lights up when on

Surprisingly, this is implemented when the block is created in AbstractBlockSettings (or Fabric's FabricBlockSettings) and not as an abstract method. The luminance parameters takes a lambda that converts blockstate to a light level:

```java
blocks.put("furnace_generator", new BlockWrapper(new FurnaceGenerator(
        FabricBlockSettings
                .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)
                .luminance(blockState -> blockState.get(FurnaceGenerator.LIT) ? 13 : 0))));
```



### Misc interactions

We also implement some Minecraft-y behaviors to fit with the game:

- Prevent pistons from pushing the generator:

```java
@Override
public PistonBehavior getPistonBehavior(BlockState state) {
    return PistonBehavior.BLOCK;
}
```

- Allow comparators to read the inventory fullness:

```java
@Override
public boolean hasComparatorOutput(BlockState state) {
    return true;
}

@Override
public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
    return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
}
```

- Drop items inside when broken (and update comparators reading from it):

```java
@Override
public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (state.getBlock() != newState.getBlock()) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FurnaceGeneratorEntity) {
            ItemScatterer.spawn(world, pos, (FurnaceGeneratorEntity)blockEntity);
            world.updateComparators(pos,this);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
```



## Block Entities 

We need the block to have an inventory to store fuel like coal, as well as an interface for the player to see and interact with said inventory. We also need the block to interact with other blocks like hoppers (to take / insert items into the inventory), burn the fuel item and in the future, store data for voltage, power, temperature, etc...

Block entities are what Minecraft uses to store fancy data like this.

### FurnaceGenerator

Firstly, we make `FurnaceGenerator` implement `BlockEntityProvider`.

Then we spawn in a block entity whenever the block is created by defining `createBlockEntity`:

```java
@Override
public BlockEntity createBlockEntity(BlockView blockView) {
    return new FurnaceGeneratorEntity();
}
```



### FurnaceGeneratorEntity

```java
public class FurnaceGeneratorEntity extends BlockEntity implements ImplementedInventory, SidedInventory, Tickable, NamedScreenHandlerFactory {
```

Oh boy. So our block entity has:

- **ImplementedInventory** - An inventory interface by Juuz that simplifies common interactions
- **SidedInventory** - Allows restricting what slots can be accessed from what sides (by hoppers, for example)
- **Tickable** - Can run something every in-game tick
- **NamedScreenHandlerFactory** - For GUI, we'll cover it later



#### Hopper interactions

Here we restrict hoppers to:

- Inserting fuel from any side except the bottom, and can only insert burnable items
- Can only take from the bottom side, and can only take non-fuel items

```java
@Override
public boolean canInsert(int slot, ItemStack stack, Direction direction) {
    return direction != Direction.DOWN && AbstractFurnaceBlockEntity.canUseAsFuel(stack);
}

@Override
public boolean canExtract(int slot, ItemStack stack, Direction direction) {
    return direction == Direction.DOWN && !AbstractFurnaceBlockEntity.canUseAsFuel(stack);
}
```



#### Burning Fuel

```java
@Override
public void tick() {
    boolean dirty = false;
    boolean burningInitialState = fuelTime > 0;
    boolean burningFinalState;

    if (!this.world.isClient) {
        ItemStack itemStack = getStack(0);

        // Burning fuel logic
        if (fuelTime > 0)
            fuelTime--;
        if (fuelTime == 0) {
            dirty = true;

            if (!itemStack.isEmpty() && AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)) {
                Item itemInitial = itemStack.getItem(); // Before decrementing
                itemStack.decrement(1);
                fuelTime = AbstractFurnaceBlockEntity.createFuelTimeMap().get(itemInitial);

                if (itemStack.isEmpty()) {
                    Item itemRemainder = itemInitial.getRecipeRemainder();
                    setStack(0, itemRemainder == null ? ItemStack.EMPTY : new ItemStack(itemRemainder));
                }
            }
        }

        // Heat up if burning, cool down if not
        if (fuelTime > 0)
            temperature = Math.min(10, temperature + 0.005f);
        else
            temperature = Math.max(temperature - 0.05f, 0);

        // Burning state toggled, update blockState
        burningFinalState = fuelTime > 0;
        if (burningInitialState != burningFinalState) {
            dirty = true;
            this.world.setBlockState(this.pos,
                    this.world.getBlockState(this.pos).with(FurnaceGenerator.LIT, burningFinalState),
                    3); // Flags 1 | 2 = update block & send changes to client
        }
    }

    if (dirty)
        markDirty();
}
```





## GUI