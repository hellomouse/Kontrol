package net.hellomouse.c_interp.common.expressions.labels;

/**
 * A named label, used with goto statements
 * @author Bowserinator
 */
public class NamedLabel extends AbstractLabel {
    public final String name;

    /**
     * Construct a named label
     * @param name Name of the label, must be unique per function scope
     * @param address Address of the label
     */
    public NamedLabel(String name, int address) {
        super(address);
        this.name = name;
    }
}
