package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractBinaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Misc binary operations:
 * <ul>
 *     <li>A[B] - SUBSCRIPT</li>
 *     <li>A->B - DEREFERENCE</li>
 *     <li>A.B  - REFERENCE</li>
 *     <li>A, B - COMMA</li>
 * </ul>
 * @author Bowserinator
 */
public class MiscBinOp extends AbstractBinaryOperation {
    public enum BinOp { SUBSCRIPT, DEREFERENCE, REFERENCE, COMMA };
    public final BinOp op;

    public MiscBinOp(BinOp op, IExpression operand1, IExpression operand2, Machine machine) {
        super(getId(op), operand1, operand2, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public IRuntimeConstant runtimeEval(InterpreterState state) {
        ConstantValue val1 = (ConstantValue)operands.get(0).runtimeEval(state);
        ConstantValue val2 = (ConstantValue)operands.get(1).runtimeEval(state);

        // TODO
        return switch (op) {
            case SUBSCRIPT -> null;
            case DEREFERENCE -> null;
            case REFERENCE -> null;
            case COMMA -> val2;
        };
    }

    @Override
    public AbstractTypeStorage getType() {
        return switch (op) {
            case SUBSCRIPT -> OperandTypeChecks.subscript(operands, machine);
            case DEREFERENCE -> OperandTypeChecks.structOrUnionDereference(operands, machine);
            case REFERENCE -> OperandTypeChecks.structOrUnionReference(operands, machine);
            case COMMA -> operands.get(1).getType();
        };
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(BinOp op) {
        return switch (op) {
            case SUBSCRIPT -> 0;
            case DEREFERENCE -> 3;
            case REFERENCE -> 4;
            // case CAST -> 34;
            case COMMA -> 46;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(BinOp op) {
        return switch (op) {
            case SUBSCRIPT -> "[]";
            case DEREFERENCE -> "->";
            case REFERENCE -> ".";
            case COMMA -> ",";
        };
    }
}
