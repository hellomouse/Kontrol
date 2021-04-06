package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ternary operation: A ? B : C
 * @author Bowserinator
 */
public class TernaryOp extends AbstractOperation {
    /**
     * Construct a ternary operation
     * @param operand1 Condition statement
     * @param operand2 Result if true
     * @param operand3 Result if false
     * @param machine Machine instance
     */
    public TernaryOp(IExpression operand1, IExpression operand2, IExpression operand3, Machine machine) {
        super(31, new ArrayList<>(Arrays.asList(operand1, operand2, operand3)), machine, "?:");
    }

    @Override
    public IRuntimeConstant runtimeEval(InterpreterState state) {
        boolean truth = ((ConstantValue)operands.get(0).runtimeEval(state)).isNotZero();
        return truth ?
                operands.get(1).runtimeEval(state) :
                operands.get(2).runtimeEval(state);
    }

    @Override
    public AbstractTypeStorage getType() {
        return OperandTypeChecks.ternary(operands, machine);
    }

    @Override
    public String toString() {
        return operands.get(0) + " ? " + operands.get(1) + " : " + operands.get(2);
    }
}
