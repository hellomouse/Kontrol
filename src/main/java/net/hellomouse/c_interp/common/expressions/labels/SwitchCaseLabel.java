package net.hellomouse.c_interp.common.expressions.labels;

import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;

/**
 * Label for a case or default in a switch statement
 * @author Bowserinator
 */
public class SwitchCaseLabel extends AbstractLabel {
    public final ConstantValue value;

    /**
     * Construct a switch case label
     * @param value Value, ie case 1:, the value is 1. If this label represents
     *              a default:, set value to null
     * @param address Address of the label
     */
    public SwitchCaseLabel(ConstantValue value, int address) {
        super(address);
        this.value = value;
    }
}
