package net.hellomouse.kontrol.electrical.circuit.thermal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;


/**
 * A thermal simulation for a block entity
 * @author Bowserinator
 */
public class ThermalComponent {
    public double temperature = 22.0;
    public double thermalR;
    public double thermalC;

    private double tAmbient = 0.0;
    private double envThermalR = 0.0;
    private double heatDissipationRate = 0.0;
    private boolean temperatureSetYet = false;

    // Block => temperature in degrees C
    public static final HashMap<Block, Double> temperatureBlocks = new HashMap<>();
    public static final HashMap<Block, Double> temperatureResistanceBlocks = new HashMap<>();

    public static final double AIR_THERMAL_R = 38.65; // K / W at ~20 C, most biome temperatures are close to this value of thermalR
    public static final double WATER_THERMAL_R = 1.672; // K / W

    static {
        // Hot blocks
        temperatureBlocks.put(Blocks.LAVA, 900.0);
        temperatureBlocks.put(Blocks.FIRE, 700.0);
        temperatureBlocks.put(Blocks.SOUL_FIRE, 1100.0);
        temperatureBlocks.put(Blocks.CAMPFIRE, 315.0);
        temperatureBlocks.put(Blocks.SOUL_CAMPFIRE, 400.0);

        // Cold blocks
        temperatureBlocks.put(Blocks.SNOW_BLOCK, -2.0);
        temperatureBlocks.put(Blocks.ICE, -2.0);
        temperatureBlocks.put(Blocks.FROSTED_ICE, 0.0);
        temperatureBlocks.put(Blocks.PACKED_ICE, -10.0);
        temperatureBlocks.put(Blocks.BLUE_ICE, -30.0);

        // Blocks that alter thermalR at the boundary
        temperatureResistanceBlocks.put(Blocks.WATER, WATER_THERMAL_R);
    }

    /**
     * Construct a thermal component
     * @param thermalR Thermal resistance
     * @param thermalC Thermal capacitance
     */
    public ThermalComponent(double thermalR, double thermalC) {
        this.thermalR = thermalR;
        this.thermalC = thermalC;
    }

    /**
     * Update the local ambient temperature by checking the current biome
     * and surround blocks. tAmbient = average temperature of surrounding 6
     * block faces, non-temperature blocks default to baseBiomeTemperature
     *
     * @param world World block entity belongs to
     * @param pos Position of block entity this belongs to
     */
    public void updateAmbientTemperature(World world, BlockPos pos) {
        if (world == null || world.isClient)
            return;

        // This equation from https://www.reddit.com/r/Minecraft/comments/3eh7yu/the_rl_temperature_of_minecraft_biomes_revealed/
        // by u/brinjal66, just a fun average-based approximation of code temperature => temp in degrees C
        double baseBiomeTemperature = 13.6484805403 * world.getBiome(pos).getTemperature(pos) + 7.0879687222;
        tAmbient = envThermalR = 0.0;

        BlockState blockState = world.getBlockState(pos);
        boolean waterlogged = blockState.contains(Properties.WATERLOGGED) ?
                blockState.get(Properties.WATERLOGGED) : false;

        for (Direction dir : Direction.values()) {
            Block block = world.getBlockState(pos.offset(dir)).getBlock();
            tAmbient += temperatureBlocks.getOrDefault(block, baseBiomeTemperature);
            envThermalR += waterlogged ? WATER_THERMAL_R : temperatureResistanceBlocks.getOrDefault(block, AIR_THERMAL_R);
        }
        envThermalR /= 6;
        tAmbient /= 6;
    }

    /**
     * Tick, updating temperature simulation
     * @param world World block entity belongs to
     * @param pos Position of block entity this belongs to
     */
    public void tick(World world, BlockPos pos, double dissipatedPower) {
        // First time update
        if (!temperatureSetYet) {
            temperatureSetYet = true;
            updateAmbientTemperature(world, pos);
        }

        // thermalC = 0: Division by 0, heat transfers at max rate instantly
        // thermalR < 0: Special case component with no resistance
        // Either case snaps directly to ambient temperature
        if (thermalC == 0.0 || thermalR < 0.0) {
            temperature = tAmbient;
            return;
        }

        // Power dissipation drives the final temp to [W] * [thermalR + envThermalR] + tAmbient
        // Thermal capacitance prevents instantaneous change of power dissipation rate
        // If you consider RC circuit analogy:
        //  - V = delta K, final temp
        //  - C = thermal capacitance, J / K
        //  - R = thermal resistance, K / W
        //  - I = heat dissipation rate, W

        double thermalSource = dissipatedPower * (thermalR + envThermalR) + tAmbient;
        double oldTemp = temperature;

        temperature += 1 / thermalC * heatDissipationRate;
        heatDissipationRate = (thermalSource - temperature) / (thermalR + envThermalR);

        // Divergence check
        if ((oldTemp < temperature && temperature > thermalSource) ||
                (oldTemp > temperature && temperature < thermalSource)) {
            heatDissipationRate = 0.0;
            temperature = thermalSource;
        }
    }

    /**
     * Save to NBT Tag
     * @param tag Tag to write data to
     * @return Tag with data written
     */
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("Temperature", temperature);
        tag.putDouble("TAmbient", tAmbient);
        tag.putDouble("EnvThermalR", envThermalR);
        tag.putDouble("HeatDissipationRate", heatDissipationRate);
        tag.putDouble("ThermalR", thermalR);
        tag.putDouble("ThermalC", thermalC);
        return tag;
    }

    /**
     * Load data from NBT tag
     * @param tag Tag to load data from
     */
    public void fromTag(CompoundTag tag) {
        temperature = tag.getDouble("Temperature");
        tAmbient = tag.getDouble("TAmbient");
        envThermalR = tag.getDouble("EnvThermalR");
        heatDissipationRate = tag.getDouble("HeatDissipationRate");
        thermalR = tag.getDouble("ThermalR");
        thermalC = tag.getDouble("ThermalC");
    }
}
