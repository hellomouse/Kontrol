package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.VariableExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractUnaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Increment or decrement operation:
 * <ul>
 *     <li>++A - PRE_INC</li>
 *     <li>A++ - POST_INC</li>
 *     <li>--A - PRE_DEC</li>
 *     <li>A-- - POST_DEC</li>
 * </ul>
 * @author Bowserinator
 */
public class IncDecOp extends AbstractUnaryOperation {
    public enum IncDecOpEnum { PRE_INC, POST_INC, PRE_DEC, POST_DEC };
    public final IncDecOpEnum op;

    public IncDecOp(IncDecOpEnum op, IExpression operand, Machine machine) {
        super(getId(op), operand, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        VariableExpression varExpr = (VariableExpression)operands.get(0);
        Variable var = state.getCurrentScope().getVariable(varExpr.getRuntimeVarName());

        ConstantValue originalValue = (ConstantValue)var.getValue().runtimeEval(state);
        ConstantValue one = new ConstantValue(machine, "1");
        ConstantValue newValue = switch(op) {
            case POST_INC, PRE_INC -> one.add(originalValue);
            case POST_DEC, PRE_DEC -> originalValue.copy().sub(one);
        };

        var.setValue(newValue.postOperation(var.getType()));

        return switch(op) {
            case POST_INC, POST_DEC -> newValue;
            case PRE_INC, PRE_DEC   -> originalValue;
        };
    }

    @Override
    public AbstractTypeStorage getType() {
        return OperandTypeChecks.numericOrPointer(operands, machine);
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(IncDecOpEnum op) {
        return switch (op) {
            case PRE_INC  -> 12;
            case POST_INC -> 13;
            case PRE_DEC  -> 14;
            case POST_DEC -> 15;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(IncDecOpEnum op) {
        return switch (op) {
            case PRE_INC, POST_INC -> "++";
            case PRE_DEC, POST_DEC -> "--";
        };
    }

    @Override
    public String toString() {
        return switch (op) {
            case PRE_INC, PRE_DEC   -> getOperator(op) + operands.get(0);
            case POST_INC, POST_DEC -> operands.get(0) + getOperator(op);
        };
    }
}
