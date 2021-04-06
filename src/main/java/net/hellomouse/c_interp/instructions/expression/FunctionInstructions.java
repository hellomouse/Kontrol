package net.hellomouse.c_interp.instructions.expression;

import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.FunctionParameter;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.scope.LocalScope;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.instructions.IJumpInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.ArrayList;
import java.util.StringJoiner;

public class FunctionInstructions {
    public static class FunctionDefinitionInstruction extends AbstractInstruction implements IJumpInstruction {
        public final AbstractTypeStorage returnType;
        public final String name;
        public final ArrayList<FunctionParameter> parameters;
        public final FunctionTypeStorage functionTypeStorage;
        private int address = -1;

        public FunctionDefinitionInstruction(AbstractTypeStorage returnType, String name, ArrayList<FunctionParameter> parameters, FunctionTypeStorage functionTypeStorage) {
            this.returnType = returnType;
            this.name = name;
            this.parameters = parameters;
            this.functionTypeStorage = functionTypeStorage;
        }

        public String toASMLine() {
            StringJoiner joiner = new StringJoiner(",");
            for (FunctionParameter parameter : parameters)
                joiner.add(parameter.toString());
            return getInstrName() + " " + returnType + " " + name + " (" + joiner + ")";
        }

        public void interpret(InterpreterState state) {
            state.log.entryExitPoint("Function '" + name + "' at addr " + state.getInstructionPointer() + ", end at " + address);
            state.globalScope.functionDeclarations.put(name, functionTypeStorage);
            state.functionAddressMap.put(name, state.getInstructionPointer());
        }

        public void setAddress(int address) { this.address = address; }
        public int getAddress() { return address; }

        public String getInstrName() {
            return "FUNC_DEF";
        }
    }

    public static class FunctionEndInstruction extends AbstractInstruction {
        public FunctionEndInstruction() { }

        public void interpret(InterpreterState state) {
            state.log.entryExitPoint("Function end");

            // TODO; unify w/ return
            if (state.frames.size() > 0) {
                state.frames.peek().setReturnValue(new ConstantValue(state.interpreter.machine, "0"));
                state.exitFunctionScope(false);
            }
        }

        public String toASMLine() {
            return getInstrName();
        }

        public String getInstrName() {
            return "FUNC_END";
        }
    }

    public static class LoadFunctionArguments extends AbstractInstruction {
        public final String funcName;

        public LoadFunctionArguments(String funcName) {
            this.funcName = funcName;
        }

        public void interpret(InterpreterState state) {
            state.log.entryExitPoint("function call start");

            // Function scope TODO explain
            // TODO: also scope id = funcName for exitFunctionOn
            state.scopes.pop();
            state.scopes.add(new LocalScope(state.globalScope, funcName));
            state.interpreter.listeners.onEnterFunction(funcName);

            Variable[] arguments = state.frames.peek().arguments;
            for (Variable argument : arguments) {
                state.addVariable(argument);
            }
        }

        public String toASMLine() {
            return getInstrName();
        }

        public String getInstrName() {
            return "FUNC_LOAD";
        }
    }
}
