package net.hellomouse.c_interp.instructions;

import net.hellomouse.c_interp.interpreter.InterpreterState;

public abstract class AbstractInstruction {
    public abstract String toASMLine();
    public abstract String getInstrName();

    public abstract void interpret(InterpreterState interpreter);
}
