package net.hellomouse.c_interp.interpreter.stack;

import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.Variable;

public class StackFrame {
    public final int startAddress;
    public final Variable[] arguments;
    private IRuntimeConstant returnValue;

    public StackFrame(int startAddress, Variable[] arguments) {
        this.startAddress = startAddress;
        this.arguments = arguments;
    }

    public void setReturnValue(IRuntimeConstant value) { this.returnValue = value; }
    public IRuntimeConstant getReturnValue() { return returnValue; }
}
