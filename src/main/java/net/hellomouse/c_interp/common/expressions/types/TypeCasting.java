package net.hellomouse.c_interp.common.expressions.types;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.storage_types.*;
import net.hellomouse.c_interp.common.storage_types.base.BaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.FloatBaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.IntBaseTypeStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for casting between types
 * @author Bowserinator
 */
public class TypeCasting {
    /**
     * Implicit conversion between two numeric types in an operation.
     * The rules for conversion are as follows:
     * <ol>
     *     <li>If the lower cast priority type is unsigned and higher cast priority is signed:
     *          <ol>
     *              <li>If max value of signed >= max value of unsigned, return <b>signed type</b><br>
     *                  <i>Example: signed int + unsigned char => signed int</i></li>
     *              <li>Else return <b>unsigned variant of signed type</b><br>
     *                  <i>Example: UL + LL = ULL (If long long cannot hold all values of unsigned long)</i></li>
     *          </ol>
     *     </li>
     *     <li>Else return <b>type with higher cast priority<br>
     *         <i>Example: signed long + signed int => signed long</i></b>
     *     </li>
     * </ol>
     *
     * <p>The cast priority order goes char, short, int, long, long long, float, double, long double. Unsigned variants
     * have cast higher, ie unsigned char has priority over signed char, but do not have a higher priority over the next
     * type in the list, ie unsigned char has lower priority than signed short.</p>
     *
     * <p>If one type is null, the other will be returned. If both types are null this will throw an IllegalStateException</p>
     *
     * @param A Type A
     * @param B Type B
     * @param machine Machine instance
     * @return Promoted type for both A and B in the operation
     */
    public static BaseTypeStorage numericPromote(@Nullable BaseTypeStorage A, @Nullable BaseTypeStorage B, Machine machine) {
        if (A == null && B == null)
            throw new IllegalStateException("Cannot numericPromote two null types");

        if (A == null) return B;
        if (B == null) return A;

        if (A instanceof IntBaseTypeStorage && B instanceof IntBaseTypeStorage) {
            IntBaseTypeStorage lesser = (IntBaseTypeStorage)(A.castPriority < B.castPriority ? A : B);
            IntBaseTypeStorage greater = (IntBaseTypeStorage)(A.castPriority > B.castPriority ? A : B);

            if (lesser.isUnsigned() && !greater.isUnsigned()) {
                if (greater.getMaxValue().compareTo(lesser.getMaxValue()) >= 0)
                    return greater;
                return machine.primitives.toUnsignedIntTypeReference(greater);
            }
        }
        return A.castPriority < B.castPriority ? B : A;
    }

    /**
     * Can type A be cast to type B?
     * @param A Type A
     * @param B Type B
     * @param machine Machine instance
     * @return Can A be cast to B
     */
    public static boolean areTypesCompatible(AbstractTypeStorage A, AbstractTypeStorage B, Machine machine) {
        return commonType(A, B, machine) != null;
    }

    /**
     * Is given type a numeric type? Numeric types include integer and floating types,
     * but not pointer types.
     * @param A Type to check
     * @return Is A a numeric type
     */
    public static boolean isNumericType(AbstractTypeStorage A) {
        return isIntegerType(A) || (A instanceof FloatBaseTypeStorage);
    }

    /**
     * Is the given type a numeric or pointer type?
     * @param A Type to check
     * @return Is A a numeric or pointer type
     * @see TypeCasting#isNumericType(AbstractTypeStorage)
     */
    public static boolean isNumericOrPointerType(AbstractTypeStorage A) {
        return A instanceof PointerTypeStorage || isNumericType(A);
    }

    /**
     * Is the given type an integer type
     * @param A Type to check
     * @return Is A an integer type
     */
    public static boolean isIntegerType(AbstractTypeStorage A) {
        return A instanceof IntBaseTypeStorage;
    }

    /**
     * <p>Return a common type between types that can be implicitly converted. This is more
     * flexible than numericPromote as it can convert between pointer types and function
     * types as well.</p><br>
     *
     * <p><b>Compatible type checks:</b><br>
     * <ul>
     *     <li>The types are the same</li>
     *     <li>They are pointed pointing to compatible types</li>
     *     <li>They are array types with:<br>
     *         <ul>
     *             <li>Compatible element types</li>
     *             <li>Sizes are same, or VLA / unknown length</li>
     *         </ul></li>
     *     <li>They are struct or union types with:<br>
     *         <ul>
     *              <li>Must have same member types, names, and for structs, declaration order.</li>
     *              <li>Tag names must be the same (ie struct x {...} != struct y {...}, even if ... is the same)</li>
     *              <li>Corresponding bit-fields must be the same size</li>
     *         </ul></li>
     *     <li>They are an enum with:<br>
     *         <ul>
     *             <li>Must be int or enum type</li>
     *         </ul></li>
     *     <li>They are functions with:<br>
     *         <ul>
     *             <li>Compatible return type</li>
     *             <li>Compatible parameter list, including varargs (...)</li>
     *         </ul></li>
     * </ul></p>
     *
     * @param A Type A
     * @param B Type B
     * @param machine Machine instance
     * @return Common type, or promoted type in the cast of numeric values.
     *         Returns null if no common type was found.
     */
    public static AbstractTypeStorage commonType(AbstractTypeStorage A, AbstractTypeStorage B, Machine machine) {
        boolean A_integer = A instanceof IntBaseTypeStorage;
        boolean B_integer = B instanceof IntBaseTypeStorage;


        // TODO: make correct
        // If both types are array types, the following rules are applied:
            //If one type is an array of known constant size, the composite type is an array of that size.
            //Otherwise, if one type is a VLA whose size is specified by an expression that is not evaluated, the behavior is undefined.
            //Otherwise, if one type is a VLA whose size is specified, the composite type is a VLA of that size.
            //Otherwise, if one type is a VLA of unspecified size, the composite type is a VLA of unspecified size.
            //(since C99)
            //Otherwise, both types are arrays of unknown size and the composite type is an array of unknown size.
        //The element type of the composite type is the composite type of the two element types.
            //If only one type is a function type with a parameter type list (a function prototype), the composite type is a function prototype with the parameter type list.
            //If both types are function types with parameter type lists, the type of each parameter in the composite parameter type list is the composite type of the corresponding parameters.

        // Any integer <=> Any integer
        if (A_integer && B_integer) return numericPromote((BaseTypeStorage)A, (BaseTypeStorage)B, machine);

        boolean A_pointer = A instanceof PointerTypeStorage;
        boolean B_pointer = B instanceof PointerTypeStorage;

        // Function pointers
        //      there are no conversions between pointers to functions and pointers to objects (including void*)
        //      there are no conversions between pointers to functions and integers
        if (A instanceof PointerTypeStorage || B instanceof PointerTypeStorage) {
            boolean PA_func = A instanceof PointerTypeStorage && ((PointerTypeStorage)A).baseType instanceof FunctionTypeStorage;
            boolean PB_func = B instanceof PointerTypeStorage && ((PointerTypeStorage)B).baseType instanceof FunctionTypeStorage;

            if (PA_func || PB_func) {
                if (!(PA_func && PB_func)) {
                    // TODO: error
                    return null;
                }
                return A;
            }
        }

        // Pointers of compatible type
        if (A_pointer && B_pointer) {
            PointerTypeStorage PA = (PointerTypeStorage)A;
            PointerTypeStorage PB = (PointerTypeStorage)B;

            // function <=> function
            if (PA.baseType instanceof FunctionTypeStorage && PB.baseType instanceof FunctionTypeStorage)
                return PA;
            // object <=> object
            if (PA.baseType instanceof StructOrUnionStorage && PB.baseType instanceof StructOrUnionStorage)
                return PA;
            // object <=> object
            if (PA.baseType instanceof ArrayTypeStorage && PB.baseType instanceof ArrayTypeStorage)
                return PA;
        }

        // Any integer <=> Any pointer
        if ((A_integer || A_pointer) && (B_integer || B_pointer) && !(A_pointer && B_pointer)) return A;

        // Float <=> Float
        if (isNumericType(A) && isNumericType(B))
            //noinspection ConstantConditions
            return numericPromote((BaseTypeStorage)A, (BaseTypeStorage)B, machine);

        // Same type
        if (A.equals(B)) return A;

        return null;
    }
}
