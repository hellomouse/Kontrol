# Thermal Simulation

This page contains detailed dev notes on the thermal simulation of components. This is not an *entirely* accurate simulation, but is a decent approximation that still remains relatively realistic. Thermal simulation can be found in `circuit/thermal`.



## Core Simulation

Each component's thermal calculation is simulated as an RC circuit. The following analogies may help[^1]

| Component      | Thermal analog                                           | Unit   |
| -------------- | -------------------------------------------------------- | ------ |
| Voltage source | Temperature difference from 0K                           | Kelvin |
| Capacitor      | Thermal capacitance (heat stored in component)           | J / K  |
| Resistor       | Thermal resistance (how difficult it is to conduct heat) | K / W  |
| Current        | Heat dissipation rate (how fast energy is removed)       | W      |

The thermal source (voltage source analog) is the final temperature, determined by:

```
T_final = T_ambient + powerDissipated * (R_component + R_ambient)
```

To cool down a component, either `T_ambient` can be lowered (put a component in a cool environment) or `R_ambient` can be decreased (use a heatsink or surround it with a more heat conductive material)

Note t thermal capacitance only affects how fast the component reaches its final temperature.

[^1]: See http://ngspice.sourceforge.net/ngspice-electrothermal-tutorial.html



## Ambient Influences

Some blocks have a fixed temperature that will affect the ambient temperature of a block. The ambient temperature of a block is averaged from the temperature of all blocks in the 6 surrounding faces. Most blocks do not have a defined temperature, so they default to the biome's ambient temperature.

**Hot Blocks:**

| Block         | Temperature (C) |
| ------------- | --------------- |
| LAVA          | 900             |
| FIRE          | 700             |
| SOUL_FIRE     | 1100            |
| CAMPFIRE      | 315             |
| SOUL_CAMPFIRE | 400             |

**Cold Blocks:**

| Block       | Temperature (C) |
| ----------- | --------------- |
| SNOW_BLOCK  | -2              |
| ICE         | -2              |
| FROSTED_ICE | 0               |
| PACKED_ICE  | -10             |
| BLUE_ICE    | -30             |

Above is a list of blocks with fixed temperature, and the temperature in degrees C (this data may be outdated at time of writing, and can be modified with other mods)

---

The biome's ambient temperature (in degrees C) is calculated using the following formula:

```
baseBiomeTemperature = 13.6484805403 * biomeTemperature + 7.0879687222;
```

Where biomeTemperature is the Minecraft temperature for each biome (see [this](https://minecraft.gamepedia.com/Biome#Temperature)). This equation from https://www.reddit.com/r/Minecraft/comments/3eh7yu/the_rl_temperature_of_minecraft_biomes_revealed/

---

In addition, the environmental thermal resistance can be altered. By default this value is `AIR_THERMAL_R` (it is always `WATER_THERMAL_R ` if waterlogged regardless of surroundings). The thermal R is averaged by the surrounding block's thermalR, or just `AIR_THERMAL_R` by default.