package net.hellomouse.c_interp.common.expressions.types;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.PointerTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.compiler.Compiler;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Functions for type checking operations at compile time
 * @author Bowserinator
 */
public class TypeCheck {
    /**
     * Type check an initialization expression (ie, int myVar = expression)
     * @param compiler Compiler instance
     * @param rightContext Context for the right hand expression, used for error message
     * @param left Type of left hand expression (ie, int)
     * @param right Type of what the left hand expression is being assigned to
     */
    public static void checkInit(Compiler compiler, ParseTree rightContext, AbstractTypeStorage left, AbstractTypeStorage right) {
        if (!TypeCasting.areTypesCompatible(left, right, compiler.machine))
            compiler.error("incompatible types when initializing type '" + left.getFullName() + "' using type '" + right.getFullName() + "'", rightContext);
    }

    /**
     * Type check an assignment expression (ie, myVar = expression)
     * @param compiler Compiler instance
     * @param rightContext Context for the right hand expression, used for error message
     * @param left Type of left hand expression (ie, the type of myVar, *myVar, a->b)
     * @param right Type of what the left hand expression is being assigned to
     */
    public static void checkAssignment(Compiler compiler, ParseTree rightContext, AbstractTypeStorage left, AbstractTypeStorage right) {
        if (!TypeCasting.areTypesCompatible(left, right, compiler.machine))
            compiler.error("incompatible types when assigning type '" + left.getFullName() + "' from type '" + right.getFullName() + "'", rightContext);
    }

    /**
     * Verify an unary operator is scalar
     * @param compiler Compiler instance
     * @param symbolContext ParseTree for the operator symbol, used for error message
     * @param operandType Operand type
     * @param opName Name of the operator
     */
    public static void checkScalarUnary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage operandType, String opName) {
        if (!operandType.isScalar())
            compiler.error("wrong type argument to " + opName, symbolContext);
    }

    /**
     * Type check a pointer indirection
     * @param compiler Compiler instance
     * @param symbolContext ParseTree for the operator symbol, used for error message
     * @param operandType Operand type
     */
    public static void checkPointerIndirection(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage operandType) {
        if (!(operandType instanceof PointerTypeStorage))
            compiler.error("invalid type argument of unary '*' (have '" + operandType.getFullName() + "')", symbolContext);
    }

    /**
     * Type check an integer unary operator
     * @param compiler Compiler instance
     * @param symbolContext ParseTree for the operator symbol, used for error message
     * @param operandType Operand type
     * @param opName Name of the operator
     */
    public static void checkIntegerUnary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage operandType, String opName) {
        if (!TypeCasting.isIntegerType(operandType))
            compiler.error("wrong type argument to " + opName, symbolContext);
    }

    /**
     * Type check a numeric unary operator
     * @param compiler Compiler instance
     * @param symbolContext ParseTree for the operator symbol, used for error message
     * @param operandType Operand type
     * @param opName Name of the operator
     */
    public static void checkNumericUnary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage operandType, String opName) {
        if (!TypeCasting.isNumericType(operandType))
            compiler.error("wrong type argument to " + opName, symbolContext);
    }

    /**
     * Type check a numeric or pointer unary operator
     * @param compiler Compiler instance
     * @param symbolContext ParseTree for the operator symbol, used for error message
     * @param operandType Operand type
     * @param opName Name of the operator
     */
    public static void checkNumericOrPointerUnary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage operandType, String opName) {
        if (!TypeCasting.isNumericOrPointerType(operandType))
            compiler.error("wrong type argument to " + opName, symbolContext);
    }

    /**
     * Check if a binary operator contains only scalar types for its operands. The error message
     * differentiates between struct/union and left/right operands. Note arrays are considered
     * scalar as they can be auto-cast into pointers.
     *
     * @param compiler Compiler instance
     * @param leftContext ParseTree for left operand
     * @param rightContext ParseTree for right operand
     * @param left Left operand type
     * @param right Right operand type
     */
    public static void checkScalarBinaryIndividual(Compiler compiler, ParseTree leftContext, ParseTree rightContext, AbstractTypeStorage left, AbstractTypeStorage right) {
        if (left instanceof StructOrUnionStorage && ((StructOrUnionStorage)left).isStruct)
            compiler.error("used struct type value where scalar is required", leftContext);
        else if (left instanceof StructOrUnionStorage && !((StructOrUnionStorage)left).isStruct)
            compiler.error("used union type value where scalar is required", leftContext);
        else if (right instanceof StructOrUnionStorage && ((StructOrUnionStorage)right).isStruct)
            compiler.error("used struct type value where scalar is required", rightContext);
        else if (right instanceof StructOrUnionStorage && !((StructOrUnionStorage)right).isStruct)
            compiler.error("used union type value where scalar is required", rightContext);
    }

    /**
     * Type check a binary operator for scalar operands only
     * @param compiler Compiler instance
     * @param symbolContext ParseTree of the symbol
     * @param left Left operand type
     * @param right Right operand type
     * @param opSymbol Symbol for the operation
     */
    public static void checkScalarBinary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage left, AbstractTypeStorage right, String opSymbol) {
        if (left instanceof StructOrUnionStorage || right instanceof StructOrUnionStorage)
            compiler.error("invalid operands to binary " + opSymbol + " (have '" + left.getFullName() + "' and '" + right.getFullName() + "')", symbolContext);
    }

    /**
     * Type check a binary operator for numeric or pointer operands only
     * @param compiler Compiler instance
     * @param symbolContext ParseTree of the symbol
     * @param left Left operand type
     * @param right Right operand type
     * @param opSymbol Symbol for the operation
     */
    public static void checkNumericOrPointerBinary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage left, AbstractTypeStorage right, String opSymbol) {
        if (!TypeCasting.isNumericOrPointerType(left) || !TypeCasting.isNumericOrPointerType(right))
            compiler.error("invalid operands to binary " + opSymbol + " (have '" + left.getFullName() + "' and '" + right.getFullName() + "')", symbolContext);
    }

    /**
     * Type check a binary operator for numeric operands only
     * @param compiler Compiler instance
     * @param symbolContext ParseTree of the symbol
     * @param left Left operand type
     * @param right Right operand type
     * @param opSymbol Symbol for the operation
     */
    public static void checkNumericBinary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage left, AbstractTypeStorage right, String opSymbol) {
        if (!TypeCasting.isNumericType(left) || !TypeCasting.isNumericType(right))
            compiler.error("invalid operands to binary " + opSymbol + " (have '" + left.getFullName() + "' and '" + right.getFullName() + "')", symbolContext);
    }

    /**
     * Type check a binary operator for integer operands only
     * @param compiler Compiler instance
     * @param symbolContext ParseTree of the symbol
     * @param left Left operand type
     * @param right Right operand type
     * @param opSymbol Symbol for the operation
     */
    public static void checkIntegerBinary(Compiler compiler, ParseTree symbolContext, AbstractTypeStorage left, AbstractTypeStorage right, String opSymbol) {
        if (!TypeCasting.isIntegerType(left) || !TypeCasting.isIntegerType(right))
            compiler.error("invalid operands to binary " + opSymbol + " (have '" + left.getFullName() + "' and '" + right.getFullName() + "')", symbolContext);
    }

    /**
     * Type check a ternary operator, first operand must be scalar, second and third
     * must be of compatible type
     *
     * @param compiler Compiler instance
     * @param symbol1Context ParseTree of the "?" symbol
     * @param symbol2Context ParseTree of the ":" symbol
     * @param type1 First operand type
     * @param type2 Second operand type
     * @param type3 Third operand type
     */
    public static void checkTernary(Compiler compiler, ParseTree symbol1Context, ParseTree symbol2Context, AbstractTypeStorage type1, AbstractTypeStorage type2, AbstractTypeStorage type3) {
        if (type1 instanceof StructOrUnionStorage && ((StructOrUnionStorage)type1).isStruct)
            compiler.error("error: used struct type value where scalar is required", symbol1Context);
        else if (type1 instanceof StructOrUnionStorage && !((StructOrUnionStorage)type1).isStruct)
            compiler.error("error: used union type value where scalar is required", symbol1Context);
        else if (!TypeCasting.areTypesCompatible(type2, type3, compiler.machine))
            compiler.error("type mismatch in conditional expression", symbol2Context);
    }
}
