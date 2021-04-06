package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.VariableExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractBinaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Assignment operators:
 * <ul>
 *     <li>A = B   - ASSIGN</li>
 *     <li>A += B  - ASSIGN_ADD</li>
 *     <li>A -= B  - ASSIGN_SUB</li>
 *     <li>A *= B  - ASSIGN_MUL</li>
 *     <li>A /= B  - ASSIGN_DIV</li>
 *     <li>A %= B  - ASSIGN_MOD</li>
 *     <li>A &= B  - ASSIGN_AND</li>
 *     <li>A |= B  - ASSIGN_OR</li>
 *     <li>A ^= B  - ASSIGN_XOR</li>
 *     <li>A <<= B - ASSIGN_LSHIFT</li>
 *     <li>A >>= B - ASSIGN_RSHIFT</li>
 * </ul>
 * @author Bowserinator
 */
public class AssignmentOp extends AbstractBinaryOperation {
    public enum AssignOp { ASSIGN, ASSIGN_ADD, ASSIGN_SUB, ASSIGN_MUL, ASSIGN_DIV, ASSIGN_MOD, ASSIGN_AND, ASSIGN_OR, ASSIGN_XOR, ASSIGN_LSHIFT, ASSIGN_RSHIFT };
    public final AssignOp op;

    public AssignmentOp(AssignOp op, IExpression operand1, IExpression operand2, Machine machine) {
        super(getId(op), operand1, operand2, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        VariableExpression varExpr = (VariableExpression)operands.get(0);
        Variable var = state.getCurrentScope().getVariable(varExpr.getRuntimeVarName());

        // TODO: struct = struct

        ConstantValue val1 = (ConstantValue)var.getValue();
        ConstantValue val2 = (ConstantValue)operands.get(1).runtimeEval(state);
        state.interpreter.listeners.onVariableChange(var);

        switch (op) {
            case ASSIGN -> var.setValue(val2);
            case ASSIGN_ADD -> var.setValue(val1.add(val2));
            case ASSIGN_SUB -> var.setValue(val1.sub(val2));
            case ASSIGN_MUL -> var.setValue(val1.mul(val2));
            case ASSIGN_DIV -> var.setValue(val1.div(val2));
            case ASSIGN_MOD -> var.setValue(val1.mod(val2));
            case ASSIGN_AND -> var.setValue(val1.bitwiseAnd(val2));
            case ASSIGN_OR  -> var.setValue(val1.bitwiseOr(val2));
            case ASSIGN_XOR -> var.setValue(val1.bitwiseXor(val2));
            case ASSIGN_LSHIFT -> var.setValue(val1.shiftLeft(val2));
            case ASSIGN_RSHIFT -> var.setValue(val1.shiftRight(val2));
        };

        return ((ConstantValue)var.getValue()).postOperation(var.getType());
    }

    @Override
    public AbstractTypeStorage getType() {
        return switch (op) {
            case ASSIGN -> operands.get(0).getType();
            case ASSIGN_ADD, ASSIGN_SUB -> OperandTypeChecks.numericOrPointer(operands, machine);
            case ASSIGN_MUL, ASSIGN_DIV -> OperandTypeChecks.numeric(operands, machine);
            case ASSIGN_MOD, ASSIGN_LSHIFT, ASSIGN_RSHIFT, ASSIGN_XOR, ASSIGN_OR, ASSIGN_AND -> OperandTypeChecks.integer(operands, machine);
        };
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(AssignOp op) {
        return switch (op) {
            case ASSIGN -> 35;
            case ASSIGN_ADD -> 36;
            case ASSIGN_SUB -> 37;
            case ASSIGN_MUL -> 38;
            case ASSIGN_DIV -> 39;
            case ASSIGN_MOD -> 40;
            case ASSIGN_AND -> 41;
            case ASSIGN_OR  -> 42;
            case ASSIGN_XOR -> 43;
            case ASSIGN_LSHIFT -> 44;
            case ASSIGN_RSHIFT -> 45;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(AssignOp op) {
        return switch (op) {
            case ASSIGN -> "=";
            case ASSIGN_ADD -> "+=";
            case ASSIGN_SUB -> "-=";
            case ASSIGN_MUL -> "*=";
            case ASSIGN_DIV -> "/=";
            case ASSIGN_MOD -> "%=";
            case ASSIGN_AND -> "&=";
            case ASSIGN_OR  -> "|=";
            case ASSIGN_XOR -> "^=";
            case ASSIGN_LSHIFT -> "<<=";
            case ASSIGN_RSHIFT -> ">>=";
        };
    }

    /**
     * Return an op code from a string
     * @param op String for op code
     * @return Op code
     */
    public static AssignOp getOperatorBySymbol(String op) {
        return switch (op) {
            case "=" -> AssignOp.ASSIGN;
            case "+=" -> AssignOp.ASSIGN_ADD;
            case "-=" -> AssignOp.ASSIGN_SUB;
            case "*=" -> AssignOp.ASSIGN_MUL;
            case "/=" -> AssignOp.ASSIGN_DIV;
            case "%=" -> AssignOp.ASSIGN_MOD;
            case "&=" -> AssignOp.ASSIGN_AND;
            case "|=" -> AssignOp.ASSIGN_OR;
            case "^=" -> AssignOp.ASSIGN_XOR;
            case "<<=" -> AssignOp.ASSIGN_LSHIFT;
            case ">>=" -> AssignOp.ASSIGN_RSHIFT;
            default -> throw new IllegalStateException("Unknown operator " + op);
        };
    }
}
