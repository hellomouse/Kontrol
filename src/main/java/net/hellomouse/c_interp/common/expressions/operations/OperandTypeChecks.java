package net.hellomouse.c_interp.common.expressions.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.StructOrUnionMemberExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.types.TypeCasting;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.ArrayTypeStorage;
import net.hellomouse.c_interp.common.storage_types.PointerTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.base.BaseTypeStorage;

import java.util.ArrayList;

public class OperandTypeChecks {
    public static AbstractTypeStorage subscript(ArrayList<IExpression> expressions, Machine machine) {
        AbstractTypeStorage type = expressions.get(0).getType();
        if (!(type instanceof ArrayTypeStorage))
            throw new IllegalStateException("Expression type must be an array type, got " + type);
        return ((ArrayTypeStorage)type).elementType;
    }

    public static AbstractTypeStorage pointerIndirection(ArrayList<IExpression> expressions, Machine machine) {
        AbstractTypeStorage type = expressions.get(0).getType();
        if (!(type instanceof PointerTypeStorage))
            throw new IllegalStateException("Expression type must be a pointer type, got " + type);

        PointerTypeStorage ptype =( PointerTypeStorage)type;
        if (ptype.pointerCount > 1)
            return new PointerTypeStorage(ptype, ptype.pointerCount - 1);
        return ptype.baseType;
    }

    public static AbstractTypeStorage structOrUnionReference(ArrayList<IExpression> expressions, Machine machine) {
        AbstractTypeStorage type1 = expressions.get(0).getType();

        if (!(type1 instanceof StructOrUnionStorage))
            throw new IllegalStateException("1st operand must be a struct or union type, got " + type1);
        if (!(expressions.get(1) instanceof StructOrUnionMemberExpression))
            throw new IllegalStateException("2nd operand must be a struct or union member expression, got " + expressions.get(1));

        StructOrUnionStorage stype = (StructOrUnionStorage)type1;
        return stype.fields.get(stype.valueIndexMap.get( ((StructOrUnionMemberExpression)expressions.get(1)).name )).type;
    }

    public static AbstractTypeStorage structOrUnionDereference(ArrayList<IExpression> expressions, Machine machine) {
        AbstractTypeStorage type1 = expressions.get(0).getType();
        if (!(type1 instanceof PointerTypeStorage))
            throw new IllegalStateException("1st operand must be a struct or union pointer type, got " + type1);
        type1 = ((PointerTypeStorage)type1).baseType;

        if (!(type1 instanceof StructOrUnionStorage))
            throw new IllegalStateException("1st operand must be a struct or union pointer type, got " + type1);
        if (!(expressions.get(1) instanceof StructOrUnionMemberExpression))
            throw new IllegalStateException("2nd operand must be a struct or union member expression, got " + expressions.get(1));

        StructOrUnionStorage stype = (StructOrUnionStorage)type1;
        return stype.fields.get(stype.valueIndexMap.get( ((StructOrUnionMemberExpression)expressions.get(1)).name )).type;
    }

    public static AbstractTypeStorage numericOrPointer(ArrayList<IExpression> expressions, Machine machine) {
        BaseTypeStorage returned = null;

        for (IExpression expression : expressions) {
            AbstractTypeStorage type = expression.getType();

            if (!TypeCasting.isNumericOrPointerType(type))
                throw new IllegalStateException("Expected numeric or pointer base type, got " + type);

            if (type instanceof PointerTypeStorage)
                type = machine.primitives.getBaseType("int");

            BaseTypeStorage btype = (BaseTypeStorage)type;
            returned = TypeCasting.numericPromote(btype, returned, machine);
        }
        return returned;
    }

    public static AbstractTypeStorage numeric(ArrayList<IExpression> expressions, Machine machine) {
        BaseTypeStorage returned = null;
        for (IExpression expression : expressions) {
            AbstractTypeStorage type = expression.getType();
            if (!TypeCasting.isNumericType(type))
                throw new IllegalStateException("Expected numeric base type, got " + type);

            BaseTypeStorage baseType = (BaseTypeStorage)type;
            returned = TypeCasting.numericPromote(baseType, returned, machine);
        }
        return returned;
    }

    public static AbstractTypeStorage integer(ArrayList<IExpression> expressions, Machine machine) {
        BaseTypeStorage returned = null;

        for (IExpression expression : expressions) {
            AbstractTypeStorage type = expression.getType();
            if (!TypeCasting.isIntegerType(type))
                throw new IllegalStateException("Expected integer base type, got " + type);

            BaseTypeStorage baseType = (BaseTypeStorage)type;
            returned = TypeCasting.numericPromote(baseType, returned, machine);
        }
        return returned;
    }

    public static AbstractTypeStorage bool(ArrayList<IExpression> expressions, Machine machine) {
        return machine.primitives.INT;
    }

    public static AbstractTypeStorage ternary(ArrayList<IExpression> expressions, Machine machine) {
        return TypeCasting.commonType(expressions.get(1).getType(), expressions.get(2).getType(), machine);
    }
}
