package net.hellomouse.c_interp.common.expressions.operations.abstracts;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;

import java.util.ArrayList;
import java.util.Collections;

/**
 * An operation on a single expression
 * @author Bowserinator
 */
public abstract class AbstractUnaryOperation extends AbstractOperation {
    /**
     * Construct an abstract unary operation
     * @param id Unique numeric id
     * @param operand Operand
     * @param machine Machine instance
     * @param operator Operator symbol, ie "&"
     */
    public AbstractUnaryOperation(int id, IExpression operand, Machine machine, String operator) {
        super(id, new ArrayList<>(Collections.singletonList(operand)), machine, operator);
    }

    public String toString() {
        return operands.get(0) + "" + operator;
    }
}
