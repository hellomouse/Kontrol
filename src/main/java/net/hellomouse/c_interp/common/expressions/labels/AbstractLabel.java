package net.hellomouse.c_interp.common.expressions.labels;

/**
 * Abstract jump label
 * @author Bowserinator
 */
public abstract class AbstractLabel {
    public final int address;

    /**
     * Construct an abstract label
     * @param address Address of the label
     */
    public AbstractLabel(int address) {
        this.address = address;
    }
}
