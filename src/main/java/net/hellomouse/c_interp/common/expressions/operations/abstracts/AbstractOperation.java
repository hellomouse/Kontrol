package net.hellomouse.c_interp.common.expressions.operations.abstracts;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

import java.util.ArrayList;

/**
 * An operation on one or more expressions
 * @author Bowserinator
 */
public abstract class AbstractOperation implements IExpression {
    public final int id;

    protected final ArrayList<IExpression> operands;
    protected final Machine machine;
    protected final String operator;

    /**
     * Construct an abstract operation
     * @param id Unique numeric id
     * @param operands Operands to operate on
     * @param machine Machine instance
     * @param operator Operator symbol. Used for debug purposes only, does not need to correspond
     *                 exactly to correct syntax, rather it should just offer a clue of what the
     *                 operation is.
     */
    public AbstractOperation(int id, ArrayList<IExpression> operands, Machine machine, String operator) {
        this.id = id;
        this.operands = operands;
        this.machine = machine;
        this.operator = operator;
    }

    /**
     * Get the type of the expression
     * @return Resultant type
     */
    public abstract AbstractTypeStorage getType();

    /**
     * Does the operation contain operands that are themselves also operations?
     * (ie, does the operation need to be recursively solved?)
     * @return Does the operation contain an operation as operand
     */
    public boolean containsNoSubOperations() {
        for (IExpression expression : operands) {
            if (expression instanceof AbstractOperation)
                return false;
        }
        return true;
    }

    /**
     * Modify an operand's value
     * @param index Operand index, starting from 0
     * @param value New operand value
     */
    public void setOperand(int index, IExpression value) {
        operands.set(index, value);
    }

    /**
     * Get all operands
     * @return Operands
     */
    public ArrayList<IExpression> getOperands() { return operands; }
}
