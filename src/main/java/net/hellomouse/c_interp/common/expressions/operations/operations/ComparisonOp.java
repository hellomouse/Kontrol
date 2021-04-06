package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractBinaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Comparison operators:
 * <ul>
 *     <li>A == B - EQUAL</li>
 *     <li>A != B - NOT_EQUAL</li>
 *     <li>A > B  - GT</li>
 *     <li>A < B  - LT</li>
 *     <li>A >= B - GT_EQ</li>
 *     <li>A <= B - LT_EQ</li>
 * </ul>
 * @author Bowserinator
 */
public class ComparisonOp extends AbstractBinaryOperation {
    public enum CompareOp { EQUAL, NOT_EQUAL, GT, LT, GT_EQ, LT_EQ };
    public final CompareOp op;

    public ComparisonOp(CompareOp op, IExpression operand1, IExpression operand2, Machine machine) {
        super(getId(op), operand1, operand2, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        ConstantValue val1 = (ConstantValue)operands.get(0).runtimeEval(state);
        ConstantValue val2 = (ConstantValue)operands.get(1).runtimeEval(state);

        return switch (op) {
            case EQUAL -> val1.equality(val2);
            case NOT_EQUAL -> val1.notEquals(val2);
            case GT -> val1.greaterThan(val2);
            case LT -> val1.lessThan(val2);
            case GT_EQ -> val1.greaterThanOrEquals(val2);
            case LT_EQ -> val1.lessThanOrEquals(val2);
        };
    }

    @Override
    public AbstractTypeStorage getType() {
        return OperandTypeChecks.bool(operands, machine);
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(CompareOp op) {
        return switch (op) {
            case EQUAL -> 16;
            case NOT_EQUAL -> 17;
            case GT -> 18;
            case LT -> 19;
            case GT_EQ -> 20;
            case LT_EQ -> 21;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(CompareOp op) {
        return switch (op) {
            case EQUAL -> "==";
            case NOT_EQUAL -> "!=";
            case LT -> "<";
            case GT -> ">";
            case LT_EQ -> "<=";
            case GT_EQ -> ">=";
        };
    }
}
