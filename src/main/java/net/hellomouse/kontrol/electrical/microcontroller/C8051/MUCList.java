package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.kontrol.Kontrol;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

/**
 * Data for the list of all microcontrollers, used for instance in the MUCMaker block
 * @author Bowserinator
 */
public class MUCList {
    // Max side length of ports when constructing, inclusive
    public static final int MAX_SIDE_LENGTH = 127;

    // All scrollable microcontrollers
    // Pair is name of microcontroller, identifier of texture to render
    public static final ArrayList<MUCData> CHOICES = new ArrayList<>();
    static {
        CHOICES.add(new MUCData(new TranslatableText("microcontroller.c8051"), new Identifier(Kontrol.MOD_ID, "textures/gui/muc_maker/c8051.png"), 256));
    }

    /** Stores data for a microcontroller */
    public static class MUCData {
        public final Text name;
        public final Identifier texture;
        public final int maxPorts;

        /**
         * Construct MUCData
         * @param name Name, as a text
         * @param texture Identifier for the texture, rendered in preview, should be 64x64
         * @param maxPorts Highest port address this MUC can have, not inclusive (as addresses start at 0)
         */
        public MUCData(Text name, Identifier texture, int maxPorts) {
            this.name = name;
            this.texture = texture;
            this.maxPorts = maxPorts;
        }
    }
}
