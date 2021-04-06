package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractBinaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * All binary mathematical operations
 * <ul>
 *     <li>A + B - ADD</li>
 *     <li>A - B - SUB</li>
 *     <li>A * B - MUL</li>
 *     <li>A / B - DIV</li>
 *     <li>A % B - MOD</li>
 *     <li>A << B - LSHIFT</li>
 *     <li>A >> B - RSHIFT</li>
 * </ul>
 * @author Bowserinator
 */
public class BinMathOp extends AbstractBinaryOperation {
    public enum MathOp { ADD, SUB, MUL, DIV, MOD, LSHIFT, RSHIFT };
    public final MathOp op;

    public BinMathOp(MathOp op, IExpression operand1, IExpression operand2, Machine machine) {
        super(getId(op), operand1, operand2, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        ConstantValue val1 = (ConstantValue)operands.get(0).runtimeEval(state);
        ConstantValue val2 = (ConstantValue)operands.get(1).runtimeEval(state);

        ConstantValue returned = switch (op) {
            case ADD -> val1.add(val2);
            case SUB -> val1.sub(val2);
            case MUL -> val1.mul(val2);
            case DIV -> val1.div(val2);
            case MOD -> val1.mod(val2);
            case LSHIFT -> val1.shiftLeft(val2);
            case RSHIFT -> val1.shiftRight(val2);
        };
        return returned.postOperation(getType());
    }

    @Override
    public AbstractTypeStorage getType() {
        return switch (op) {
            case ADD, SUB -> OperandTypeChecks.numericOrPointer(operands, machine);
            case MUL, DIV -> OperandTypeChecks.numeric(operands, machine);
            case MOD, LSHIFT, RSHIFT -> OperandTypeChecks.integer(operands, machine);
        };
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(MathOp op) {
        return switch (op) {
            case ADD -> 5;
            case SUB -> 6;
            case MUL -> 9;
            case DIV -> 10;
            case MOD -> 11;
            case LSHIFT -> 25;
            case RSHIFT -> 26;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(MathOp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case LSHIFT -> "<<";
            case RSHIFT -> ">>";
        };
    }
}
