package net.hellomouse.kontrol.electrical.microcontroller;

import net.hellomouse.kontrol.Kontrol;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data for the list of all microcontrollers, used for instance in the MUCMaker block
 * @author Bowserinator
 */
public class MUCStatic {
    // Max side length of ports when constructing, inclusive
    public static final int MAX_SIDE_LENGTH = 127;

    // Defaults for MUC Maker
    public static final int rotationIndex = 0;
    public static final int sideLength = 10;
    public static final int portLower = 0;
    public static final int portUpper = 16;
    public static final int currentMUC = 0;

    // All scrollable microcontrollers
    // Pair is name of microcontroller, identifier of texture to render
    public static final ArrayList<MUCData> CHOICES = new ArrayList<>();
    static {
        CHOICES.add(new MUCData("c8051", new TranslatableText("microcontroller.c8051"), new Identifier(Kontrol.MOD_ID, "textures/gui/muc_maker/c8051.png"), 256));
    }

    // MUC Blueprints
    public static final HashMap<String, ArrayList<String[]>> MUCBlueprints = new HashMap<>();

    /** Stores data for a microcontroller */
    public static class MUCData {
        public final String id;
        public final Text name;
        public final Identifier texture;
        public final int maxPorts;

        /**
         * Construct MUCData
         * @param id Unique id
         * @param name Name, as a text
         * @param texture Identifier for the texture, rendered in preview, should be 64x64
         * @param maxPorts Highest port address this MUC can have, not inclusive (as addresses start at 0)
         */
        public MUCData(String id, Text name, Identifier texture, int maxPorts) {
            this.id = id;
            this.name = name;
            this.texture = texture;
            this.maxPorts = maxPorts;
        }
    }
}
