package net.hellomouse.c_interp.interpreter;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.scope.GlobalScope;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.instructions.IJumpInstruction;
import net.hellomouse.c_interp.instructions.expression.FunctionInstructions;
import net.hellomouse.c_interp.interpreter.stack.StackFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.function.Function;

public class InterpreterState {
    public final Interpreter interpreter;

    public int instructionPointer = 0;

    public ArrayList<AbstractInstruction> instructions = new ArrayList<>();

    public Stack<AbstractScope> scopes = new Stack<>();

    public HashMap<String, Integer> functionAddressMap = new HashMap<>();
    public HashMap<String, Function<Variable[], IRuntimeConstant>> injectedFunctionMap = new HashMap<>();

    public final ArrayList<Variable> stack = new ArrayList<>();
    public final Stack<StackFrame> frames = new Stack<>();

    private int stackPointer = 1;
    public int usedHeap = 0;

    private boolean isRunning = true;

    public GlobalScope globalScope;

    public InterpreterLogger log = new InterpreterLogger(this);

    public InterpreterState(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.globalScope = new GlobalScope();
        scopes.add(globalScope);
    }

    public int getStackPointer() { return stackPointer; }


    public boolean isRunning() { return isRunning; }

    public void tick() {
        if (instructionPointer < 0 || instructionPointer >= instructions.size()) {
            isRunning = false;
            return;
        }
        AbstractInstruction instruction = instructions.get(instructionPointer);
        instruction.interpret(this);
        instructionPointer++;

        if (instruction instanceof FunctionInstructions.FunctionDefinitionInstruction)
            jump(((IJumpInstruction)instruction).getAddress());
    }

    public int getInstructionPointer() { return instructionPointer; }


    public void jump(int address) {
        instructionPointer = address;
    }

    public void relativeJump(int offset) {
        instructionPointer += offset;
    }

    public void addVariable(Variable variable) {
        scopes.peek().variables.put(variable.getName(), variable);
    }

    public AbstractScope getCurrentScope() {
        return scopes.peek();
    }

    public void addInstruction(AbstractInstruction instruction) {
        instructions.add(instruction);
    }

    public void incrementInstructionPointer(int size) {
        instructionPointer += size;
    }

    public void incrementStackPointer(int size) {
        stackPointer += size;
    }

    public void exitFunctionScope(boolean forcePop) {
        interpreter.listeners.onExitFunction(scopes.peek().id);

        while (scopes.size() > 0 && !(scopes.peek().parent instanceof GlobalScope)) {
            scopes.pop();
        }
        // For when last scope end instr isnt reached ie return
        if (forcePop)
            scopes.pop();

        jump(frames.peek().startAddress);
    }

    public void callFunction(String funcName, ArrayList<IExpression> arguments) {
        // TODO: remove

        if (!functionAddressMap.containsKey(funcName) && !injectedFunctionMap.containsKey(funcName))
            throw new IllegalStateException("hmm " + funcName);

        Variable[] parameters = new Variable[arguments.size()];

        FunctionTypeStorage funcType =  globalScope.functionDeclarations.get(funcName);

        if (funcType == null)
            funcType = (FunctionTypeStorage)globalScope.variables.get(funcName).getType();


        for (int i = 0; i < arguments.size(); i++) {
            String varName = i < funcType.parameters.size() ?
                    funcType.parameters.get(i).name :
                    i + "vararg";

            IExpression argument = arguments.get(i);
            parameters[i] = new Variable(varName, argument.getType(),
                    argument instanceof IRuntimeConstant ?
                            argument : argument.runtimeEval(this));
        }

        // Check injected functions
        Function<Variable[], IRuntimeConstant> injectedFunc = injectedFunctionMap.get(funcName);

        frames.push(new StackFrame(getInstructionPointer(), parameters));
        isRunning = true;

        if (injectedFunc != null) {
            frames.peek().setReturnValue(injectedFunctionMap.get(funcName).apply(parameters));
        }

        else jump(functionAddressMap.get(funcName) + 1);
    }
}

