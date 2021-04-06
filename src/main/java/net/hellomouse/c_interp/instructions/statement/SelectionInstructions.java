package net.hellomouse.c_interp.instructions.statement;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.labels.SwitchCaseLabel;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.ArrayList;


public class SelectionInstructions {

    public static class SwitchInstruction extends AbstractInstruction {
        public final IExpression expression;
        private ArrayList<SwitchCaseLabel> labels;
        private int breakAddress = -1;

        public SwitchInstruction(IExpression expression) {
            this.expression = expression;
        }

        public String toASMLine() {
            return getInstrName() + " " + expression;
        }
        public String getInstrName() {
            return "SWITCH";
        }

        public void setCases(ArrayList<SwitchCaseLabel> labels) {
            this.labels = labels;
        }

        public void setBreakAddress(int address) {
            this.breakAddress = address;
        }

        public void interpret(InterpreterState state) {
            state.log.controlFlow("switch " + expression);

            if (labels.size() == 0) {
                state.jump(breakAddress);
                return;
            }

            ConstantValue value = (ConstantValue)(expression.runtimeEval(state));
            SwitchCaseLabel defaultCase = null;

            for (SwitchCaseLabel label : labels) {
                if (label.value == null) {
                    defaultCase = label;
                    continue;
                }
                if (value.equals(label.value)) {
                    state.jump(label.address);
                    return;
                }
            }
            if (defaultCase == null) {
                state.jump(labels.get(0).address);
                return;
            }
            state.jump(defaultCase.address);
        }
    }

}
