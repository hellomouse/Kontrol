package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractBinaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Binary logic operations. Includes both regular logic and
 * bitwise logic operations:
 * <ul>
 *     <li>A && B - AND</li>
 *     <li>A || B - OR</li>
 *     <li>A & B - BITAND</li>
 *     <li>A | B - BITOR</li>
 *     <li>A ^ B - BITXOR</li>
 * </ul>
 * @author Bowserinator
 */
public class BinLogicOp extends AbstractBinaryOperation {
    public enum LogicOp { AND, OR, BITAND, BITOR, BITXOR };
    public final LogicOp op;

    public BinLogicOp(LogicOp op, IExpression operand1, IExpression operand2, Machine machine) {
        super(getId(op), operand1, operand2, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        ConstantValue val1 = (ConstantValue)operands.get(0).runtimeEval(state);
        ConstantValue val2 = (ConstantValue)operands.get(1).runtimeEval(state);

        return switch (op) {
            case AND -> val1.and(val2);
            case OR  -> val1.or(val2);
            case BITAND -> val1.bitwiseAnd(val2);
            case BITOR  -> val1.bitwiseOr(val2);
            case BITXOR -> val1.bitwiseXor(val2);
        };
    }

    @Override
    public AbstractTypeStorage getType() {
        return switch (op) {
            case AND, OR -> OperandTypeChecks.bool(operands, machine);
            case BITAND, BITOR, BITXOR -> OperandTypeChecks.integer(operands, machine);
        };
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(LogicOp op) {
        return switch (op) {
            case AND -> 22;
            case OR  -> 23;
            case BITAND -> 27;
            case BITOR  -> 28;
            case BITXOR -> 29;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(LogicOp op) {
        return switch (op) {
            case AND -> "&&";
            case OR  -> "||";
            case BITAND -> "&";
            case BITOR  -> "|";
            case BITXOR -> "^";
        };
    }
}
