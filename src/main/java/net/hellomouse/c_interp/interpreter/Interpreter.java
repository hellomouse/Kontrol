package net.hellomouse.c_interp.interpreter;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.instructions.AbstractInstruction;

import java.util.ArrayList;
import java.util.function.Function;

public class Interpreter {
    public final Machine machine;
    public final InterpreterListenerHandler listeners = new InterpreterListenerHandler();

    private final InterpreterSettings settings;

    // TODO: stack of instructions
    // seperate stack of variables

    private final InterpreterState state = new InterpreterState(this);

    public Interpreter(Machine machine, InterpreterSettings settings) {
        this.machine = machine;
        this.settings = settings;
    }

    public InterpreterState getState() { return state; }

    public void injectFunction(String name, Function<Variable[], IRuntimeConstant> function) {
        state.injectedFunctionMap.put(name, function);
    }

    // TODO: rename
    public void setInstructions(ArrayList<AbstractInstruction> instructions) {
        state.instructions = instructions;
    }

    public void interpret(int loopCount) {
        int i = 0;
        while (state.isRunning() && i < loopCount) {
            state.tick();
            i++;
        }
    }

    public Variable getVariable(String variableName) {
        System.out.println(state.scopes.peek().variables);
        if (state.scopes.size() == 0)
            return state.globalScope.getVariable(variableName);
        return state.scopes.peek().getVariable(variableName);
    }

    public void callFunction(String functionName) {
        state.callFunction(functionName, new ArrayList<>());
    }

    public void callFunction(String functionName, ArrayList<IExpression> arguments) {
        state.callFunction(functionName, arguments);
    }

    public void tick() {
        // TODO: repeat tick for lcock cycles
        state.tick();
    }

    public boolean isRunning() { return state.isRunning(); }

    public void interrupt(String functionName, ArrayList<IExpression> arguments) {
        state.callFunction(functionName, arguments);
    }
}
