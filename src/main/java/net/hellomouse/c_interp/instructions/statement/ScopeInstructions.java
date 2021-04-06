package net.hellomouse.c_interp.instructions.statement;

import net.hellomouse.c_interp.common.scope.LocalScope;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;

public class ScopeInstructions {
    public static class ScopeStartInstruction extends AbstractInstruction {
        public ScopeStartInstruction() { }
        public String toASMLine() {
            return getInstrName();
        }
        public String getInstrName() {
            return "SCOPE";
        }

        public void interpret(InterpreterState state) {
            state.log.background("new scope");
            state.scopes.push(new LocalScope(state.scopes.peek(), ""));
        }
    }

    public static class ScopeEndInstruction extends AbstractInstruction {
        public ScopeEndInstruction() {}
        public String toASMLine() {
            return getInstrName();
        }
        public String getInstrName() {
            return "SCOPE_END";
        }

        public void interpret(InterpreterState state) {
            state.log.background("end scope");
            state.scopes.pop();
        }
    }

}
