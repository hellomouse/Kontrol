package net.hellomouse.c_interp.instructions.statement;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;

public class JumpInstructions {
    public static class LabelInstruction extends AbstractInstruction {
        public final String label;

        public LabelInstruction(String label) { this.label = label; }

        public String toASMLine() {
            return getInstrName() + " " + label;
        }
        public String getInstrName() {
            return "LABEL";
        }

        public void interpret(InterpreterState state) {

        }
    }

    public static class LabeledNoOpInstruction extends AbstractInstruction {
        public final String label;
        public LabeledNoOpInstruction(String label) {
            this.label = label;
        }

        public void interpret(InterpreterState state) {
            state.log.controlFlow(label);
        }

        public String toASMLine() {
            return getInstrName() + " " + label;
        }

        public String getInstrName() {
            return "NO_OP";
        }
    }



    public static class JumpInstruction extends AbstractInstruction {
        public final String info;
        protected int address;

        public JumpInstruction(String info) {
            this(info, -1);
        }

        public JumpInstruction(String info, int address) {
            this.info = info;
            this.address = address;
        }

        public void setAddress(int address) {
            this.address = address;
        }

        public String toASMLine() {
            return getInstrName() + " " + address;
        }
        public String getInstrName() {
            return "JUMP";
        }

        public void interpret(InterpreterState state) {
            state.log.controlFlow(info);
            state.jump(address);
        }
    }

    public static class JumpIfInstruction extends JumpInstruction {
        protected final IExpression expression;

        public JumpIfInstruction(String info, IExpression expression) {
            this(info, expression, -1);
        }

        public JumpIfInstruction(String info, IExpression expression, int address) {
            super(info, address);
            this.expression = expression;
        }

        @Override
        public String toASMLine() {
            return getInstrName() + " " + address + " " + expression;
        }

        @Override
        public String getInstrName() {
            return "JUMP IF";
        }

        @Override
        public void interpret(InterpreterState state) {
            state.log.controlFlow(info);

            if (expression != null) {
                // TODO: check if struct allowed
                boolean leave = ((ConstantValue) expression.runtimeEval(state)).isNotZero();
                if (leave)
                    state.jump(address);
            }
        }
    }

    public static class JumpIfNotInstruction extends JumpIfInstruction {
        public JumpIfNotInstruction(String info, IExpression expression) {
            super(info, expression);
        }

        public JumpIfNotInstruction(String info, IExpression expression, int address) {
            super(info, expression, address);
        }

        @Override
        public String getInstrName() {
            return "JUMP IF NOT";
        }

        @Override
        public void interpret(InterpreterState state) {
            state.log.controlFlow(info);

            if (expression != null) {
                // TODO: check if struct allowed
                boolean leave = !((ConstantValue) expression.runtimeEval(state)).isNotZero();
                if (leave)
                    state.jump(address);
            }
        }
    }



    private static abstract class JumpableInstruction extends AbstractInstruction {
        protected int jumpAddress = -1;

        public JumpableInstruction() {}

        public void setJumpAddress(int jumpAddress) {
            this.jumpAddress = jumpAddress;
        }
    }

    public static class GotoInstruction extends JumpableInstruction {
        public final String label;
        public GotoInstruction(String label) { this.label = label; }

        public String toASMLine() {
            return getInstrName() + " " + label;
        }
        public String getInstrName() {
            return "GOTO";
        }

        public void interpret(InterpreterState state) {
            state.log.controlFlow("goto " + jumpAddress);
            state.jump(jumpAddress);
        }
    }



    public static class ContinueInstruction extends JumpableInstruction {
        public ContinueInstruction() {}

        public String toASMLine() {
            return getInstrName();
        }
        public String getInstrName() {
            return "CONTINUE";
        }

        public void interpret(InterpreterState state) {
            state.log.controlFlow("continue " + jumpAddress);
            state.jump(jumpAddress);
        }
    }

    public static class BreakInstruction extends JumpableInstruction {
        public BreakInstruction() {}

        public String toASMLine() { return getInstrName(); }
        public String getInstrName() { return "BREAK"; }

        public void interpret(InterpreterState state) {
            state.log.controlFlow("break " + jumpAddress);
            state.jump(jumpAddress);
        }
    }

    public static class ReturnInstruction extends AbstractInstruction {
        IExpression returnValue;

        public ReturnInstruction(IExpression returnValue) {
            this.returnValue = returnValue;
        }

        public String toASMLine() { return getInstrName(); }
        public String getInstrName() { return "RETURN " + returnValue; }

        public void interpret(InterpreterState state) {
            state.log.controlFlow("return " + returnValue);

            if (state.frames.size() > 0) {
                state.frames.peek().setReturnValue(
                    returnValue == null ?
                            new ConstantValue(state.interpreter.machine, "0") :
                            returnValue.runtimeEval(state));

                state.exitFunctionScope(true);
            }
        }
    }
}
