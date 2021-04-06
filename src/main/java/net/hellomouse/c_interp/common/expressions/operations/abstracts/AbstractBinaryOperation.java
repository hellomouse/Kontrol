package net.hellomouse.c_interp.common.expressions.operations.abstracts;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An operation between two expressions
 * @author Bowserinator
 */
public abstract class AbstractBinaryOperation extends AbstractOperation {
    /**
     * Construct an abstract binary operation
     * @param id Unique numeric ID
     * @param operand1 First operand
     * @param operand2 Second operand
     * @param machine Machine instance
     * @param operator Operator symbol (ie, "<<")
     */
    public AbstractBinaryOperation(int id, IExpression operand1, IExpression operand2, Machine machine, String operator) {
        super(id, new ArrayList<>(Arrays.asList(operand1, operand2)), machine, operator);
    }

    @Override
    public String toString() {
        return "(" + operands.get(0) + " " + operator + " " + operands.get(1) + ")";
    }
}
