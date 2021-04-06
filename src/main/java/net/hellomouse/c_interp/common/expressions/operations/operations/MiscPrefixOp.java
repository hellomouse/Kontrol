package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.VariableExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.OperandTypeChecks;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractUnaryOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.PointerTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Misc prefix expressions:
 * <ul>
 *     <li>!A - NOT</li>
 *     <li>~A - BITNOT</li>
 *     <li>&A - ADDRESS</li>
 *     <li>*A - INDIRECTION</li>
 *     <li>-A - NEG</li>
 *     <li>+A - POS</li>
 *     <li>sizeof(A) - SIZEOF</li>
 *     <li>alignof(A) - ALIGNOF</li>
 * </ul>
 * @author Bowserinator
 */
public class MiscPrefixOp extends AbstractUnaryOperation {
    public enum PrefixOp { NOT, BITNOT, SIZEOF, ALIGNOF, ADDRESS, INDIRECTION, NEG, POS };
    public final PrefixOp op;

    public MiscPrefixOp(PrefixOp op, IExpression operand, Machine machine) {
        super(getId(op), operand, machine, getOperator(op));
        this.op = op;
    }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) {
        ConstantValue val = (ConstantValue)operands.get(0).runtimeEval(state);

        if (op == PrefixOp.NOT)
            return val.not();
        if (op == PrefixOp.BITNOT)
            return val.bitwiseNot();
        if (op == PrefixOp.POS)
            return val.pos();
        if (op == PrefixOp.NEG)
            return val.neg().postOperation(operands.get(0).getType());
        if (op == PrefixOp.SIZEOF)
            throw new IllegalStateException("sizeof() cannot be evaluated at runtime");
        if (op == PrefixOp.ALIGNOF)
            return new ConstantValue(machine, "1"); // All types have alignment of one because it's an interpreter

        // *A only gets value pointed to, assignment, ie *A = 1 is handled in assignment
        if (op == PrefixOp.INDIRECTION)
            return null; // TODO

        // idk address
        // idk indirection

        VariableExpression varExpr = (VariableExpression)operands.get(0);
        Variable var = state.getCurrentScope().getVariable(varExpr.getRuntimeVarName());

        // TODO???
        return null;
    }

    @Override
    public AbstractTypeStorage getType() {
        return switch(op) {
            case INDIRECTION -> OperandTypeChecks.pointerIndirection(operands, machine);
            case ADDRESS -> new PointerTypeStorage(operands.get(0).getType(), 1);
            case NEG, POS -> OperandTypeChecks.numeric(operands, machine);
            case NOT -> OperandTypeChecks.bool(operands, machine);
            case BITNOT -> OperandTypeChecks.integer(operands, machine);
            case SIZEOF, ALIGNOF -> machine.primitives.UINT;
        };
    }

    /**
     * Returns op ID from op code
     * @param op Operation
     * @return Id
     */
    private static int getId(PrefixOp op) {
        return switch (op) {
            case NOT -> 24;
            case BITNOT -> 30;
            case SIZEOF -> 32;
            case ALIGNOF -> 33;
            case ADDRESS -> 2;
            case INDIRECTION -> 1;
            case NEG -> 8;
            case POS -> 7;
        };
    }

    /**
     * Return operator as a string from op code
     * @param op Operation
     * @return Symbol for operator
     */
    private static String getOperator(PrefixOp op) {
        return switch (op) {
            case NOT -> "!";
            case BITNOT -> "~";
            case SIZEOF -> "sizeof";
            case ALIGNOF -> "alignof";
            case ADDRESS -> "&";
            case INDIRECTION -> "*";
            case NEG -> "-";
            case POS -> "+";
        };
    }
}
