package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.expressions.interfaces.ICompileTimeValue;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.types.TypeCasting;
import net.hellomouse.c_interp.common.expressions.util.Literals;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.BaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.FloatBaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.IntBaseTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.function.BiFunction;

/**
 * A constant hat represents a literal value.
 * Can be an integer, decimal or string constant. Chars are considered
 * int types, not string (char*) types.
 * @author Bowserinator
 */
public class ConstantValue implements IExpression, ICompileTimeValue, IRuntimeConstant {
    // ZERO and ONE constants
    // TODO: define on construct
    private static final Machine _trivialMachine = new Machine(new MachineSettings());
    public static final ConstantValue ZERO = new ConstantValue(_trivialMachine, "0");
    public static final ConstantValue ONE  = new ConstantValue(_trivialMachine, "1");

    public static final BigDecimal POS_INFINITY = new BigDecimal("1e99");
    public static final BigDecimal NEG_INFINITY = new BigDecimal("-1e99");

    private final Machine machine;

    private String valueString = null;
    private BigDecimal valueDecimal = null;
    private BigInteger valueInt = null;

    private boolean isInfinite = false;
    private boolean isNaN = false;

    // A note on types:
    // This type is only the base type, numeric limits are NOT
    // respected until an assignment operation
    private AbstractTypeStorage type;

    /**
     * Construct a ConstantValue when a literal.
     * @param machine Machine instance
     * @param literal String representing an integer, floating or string literal
     */
    public ConstantValue(Machine machine, String literal) {
        this.machine = machine;

        // Char literal: 'x'
        if (literal.startsWith("'") && literal.startsWith("'") && literal.length() == 3) {
            valueInt = BigInteger.valueOf(literal.charAt(1));
            type = machine.primitives.CHAR;
            return;
        }

        // String literal: "xyz"
        String _temp_str = Literals.processStringLiteral(literal);
        if (_temp_str != null) {
            valueString = _temp_str;
            type = machine.primitives.CHAR.toPointer(1);
            return;
        }

        // Integer literal
        Literals.IntLiteralData intData = Literals.processIntegerLiterals(machine, literal);
        if (intData != null) {
            valueInt = intData.value;
            type = intData.type;
            return;
        }

        // Floating literal
        Literals.FloatLiteralData floatData = Literals.processFloatingLiterals(machine, literal);
        if (floatData != null) {
            valueDecimal = floatData.value;
            type = floatData.type;
            return;
        }

        throw new IllegalStateException("Couldn't parse literal value: " + literal);
    }

    /**
     * Copy constructor
     * @param other A constant value
     */
    public ConstantValue(ConstantValue other) {
        this.machine = other.machine;
        this.valueString = other.valueString;
        this.valueInt = other.valueInt;
        this.valueDecimal = other.valueDecimal;
        this.type = other.type;
    }

    /**
     * Construct directly from a BigInteger value, resultant
     * ConstantValue will have int type
     * @param machine Machine instance
     * @param val Integer value
     */
    public ConstantValue(Machine machine, BigInteger val) {
        this.machine = machine;
        valueInt = val;
        type = machine.primitives.INT;
    }

    /**
     * Construct directly from an int value
     * @param machine Machine instance
     * @param val Integer value
     */
    public ConstantValue(Machine machine, int val) {
        this(machine, BigInteger.valueOf(val));
    }

    /**
     * Construct directly from a BigDecimal value, resultant
     * ConstantValue will have double type
     * @param machine Machine instance
     * @param val Double value
     */
    public ConstantValue(Machine machine, BigDecimal val) {
        this.machine = machine;
        valueDecimal = val;
        type = machine.primitives.DOUBLE;
    }

    /**
     * Construct directly from an double value
     * @param machine Machine instance
     * @param val Double value
     */
    public ConstantValue(Machine machine, double val) {
        this(machine, new BigDecimal(val));
    }

    @Override
    public AbstractTypeStorage getType() {
        return type;
    }

    @Override
    public ConstantValue copy() { return new ConstantValue(this); }

    @Override
    public ConstantValue runtimeEval(InterpreterState state) { return this; }

    public int getSize() {
        return type.getSize();
        // return type.getSize();

        // TODO: chekc for exceeding size of type
//        switch (type) {
//            case machine.sett -> { return machine.settings.getCharSize(); }
//            case INT -> {
//                PrimitiveTypes.IntType signedInt = (PrimitiveTypes.IntType) machine.getType("int", TypeSpecifier.SIGNED);
//                PrimitiveTypes.IntType unsignedInt = (PrimitiveTypes.IntType) machine.getType("int", TypeSpecifier.UNSIGNED);
//
//                if (value_int.compareTo(unsignedInt.getMaxValue()) > 0)
//                    machine.logger.warning("Integer constant exceeds range of 'int'");
//                else if (value_int.compareTo(signedInt.getMinValue()) < 0)
//                    machine.logger.warning("Integer constant exceeds range of 'int'");
//                return machine.settings.getIntSize();
//            }
//            case LONG -> {
//                PrimitiveTypes.IntType signedLong   = (PrimitiveTypes.IntType) machine.getType("long", TypeSpecifier.SIGNED);
//                PrimitiveTypes.IntType unsignedLong = (PrimitiveTypes.IntType) machine.getType("long", TypeSpecifier.UNSIGNED);
//
//                if (value_int.compareTo(unsignedLong.getMinValue()) > 0)
//                    machine.logger.warning("Integer constant exceeds range of 'long'");
//                else if (value_int.compareTo(signedLong.getMinValue()) < 0)
//                    machine.logger.warning("Integer constant exceeds range of 'long'");
//                return machine.settings.getLongSize();
//            }
//            case FLOAT -> {
//                PrimitiveTypes.FloatType floatType = (PrimitiveTypes.FloatType) machine.getType("float", 0x0);
//
//                if (value_decimal.compareTo(floatType.getLargestValue()) > 0)
//                    machine.logger.warning("Floating constant exceeds range of 'float'");
//                else if (value_decimal.compareTo(floatType.getSmallestNonZeroValue()) < 0)
//                    machine.logger.warning("Floating constant exceeds range of 'float'");
//                return machine.settings.getFloatSize();
//            }
//            case DOUBLE -> {
//                PrimitiveTypes.DoubleType doubleType = (PrimitiveTypes.DoubleType) machine.getType("double", 0x0);
//
//                if (value_decimal.compareTo(doubleType.getLargestValue()) > 0)
//                    machine.logger.warning("Floating constant exceeds range of 'double'");
//                else if (value_decimal.compareTo(doubleType.getSmallestNonZeroValue()) < 0)
//                    machine.logger.warning("Floating constant exceeds range of 'double'");
//                return machine.settings.getDoubleSize();
//            }
//        }
//        return machine.settings.getPointerSize();
    }

    // TODO: post-operation check
       // - variable overflow n' shit
    // TODO: post-construction check
      // - zero, one static
    // TODO: char literal??

    public ConstantValue postOperation(AbstractTypeStorage resultantType) {
        // TODO: as types may not match, run post-op

        // Int size check
        if (valueInt != null) {
            IntBaseTypeStorage intType = (IntBaseTypeStorage)resultantType;
            valueInt = intType.normalize(valueInt);
        }
        // Float type size
        else if (valueDecimal != null) {
            FloatBaseTypeStorage floatType = (FloatBaseTypeStorage)resultantType;
            valueDecimal = floatType.normalize(valueDecimal);

            if (valueDecimal == POS_INFINITY || valueDecimal == NEG_INFINITY)
                isInfinite = true;
        }
        return this;
    }

    public void markInfinite() {
        this.isInfinite = true;
    }
    public void markNaN() {
        this.isNaN = true;
    }
    public boolean isInfinite() { return isInfinite; }
    public boolean isNaN() { return isNaN; }



    /**
     * Get a BigInteger representing this value. If the current type
     * is a floating type, the returned value will be rounded down to the nearest int.
     * String literals cannot tbe cast to a BigInteger
     * @return BigInteger value
     */
    public BigInteger getBigIntegerValue() {
        if (valueInt != null) return valueInt;
        if (valueDecimal != null) return valueDecimal.toBigInteger();
        throw new IllegalStateException("Cannot cast string literal to BigInteger");
    }

    /**
     * Get a BigDecimal representing this value. String literals cannot
     * be cast to a BigDecimal
     * @return BigDecimal value
     */
    public BigDecimal getBigDecimalValue() {
        if (valueDecimal != null) return valueDecimal;
        if (valueString != null) throw new IllegalStateException("Cannot cast string literal to numeric type");
        return new BigDecimal(valueInt);
    }

    /**
     * Get a String representing this value. String literals are directly displayed,
     * integer and floating literals will be converted to a corresponding base 10
     * string representation. Character literals will be converted to the corresponding char.
     * @return String representation
     */
    @Override
    public String getStringValue() {
        if (valueString != null)  return valueString;
        if (valueDecimal != null) return valueDecimal.toString();

        if (type.equals(machine.primitives.CHAR))
            return String.valueOf((char)valueInt.intValue());
        return valueInt.toString();
    }


    /** Is the constant an integer type */
    public boolean isInt() { return valueInt != null; }

    /** Is the constant an integer or floating type */
    public boolean isNumeric() { return valueDecimal != null || valueInt == null; }

    /** Is the constant a string type (Not including char) */
    public boolean isString() { return valueString != null; }

    /**
     * Is the current value not zero?, ie value != 0:
     * <ul>
     *     <li><i>If the current type is a string:</i> Always returns true</li>
     *     <li><i>If the current type is an integer or floating type:</i> Returns value != 0</li>
     * </ul>
     * @return Is current value not zero
     */
    public boolean isNotZero() {
        if (valueString != null) return true;
        return (valueDecimal != null && !valueDecimal.equals(new BigDecimal(0))) ||
               (valueInt != null && !valueInt.equals(BigInteger.valueOf(0)));
    }




    public ConstantValue not() {
        return isNotZero() ? ZERO : ONE;
    }

    public ConstantValue and(ConstantValue other) {
        return isNotZero() && other.isNotZero() ? ONE : ZERO;
    }

    public ConstantValue or(ConstantValue other) {
        return isNotZero() || other.isNotZero() ? ONE : ZERO;
    }


    public ConstantValue neg() {
        if (valueString != null)
            throw new IllegalStateException("Invalid operands to binary -");
        if (valueInt != null)
            valueInt = valueInt.negate();
        else if (isInfinite)
            valueDecimal = valueDecimal == POS_INFINITY ? NEG_INFINITY : POS_INFINITY;
        else if (valueDecimal != null)
            valueDecimal = valueDecimal.negate();
        return this;
    }

    public ConstantValue pos() {
        if (valueString != null)
            throw new IllegalStateException("Invalid operands to binary +");
        if (isInfinite)
            this.valueDecimal = POS_INFINITY;
        return this;
    }

    public ConstantValue bitwiseNot() {
        if (valueString != null)
            throw new IllegalStateException("Invalid operands to binary ~");
        if (valueDecimal != null)
            throw new IllegalStateException("Wrong type argument to bit-complement");
        if (valueInt != null)
            valueInt = valueInt.not();
        return this;
    }


    public ConstantValue add(ConstantValue other) {
        applyDecIntMathOperator(other, "+", Operators::add, Operators::add);
        return this;
    }

    public ConstantValue sub(ConstantValue other) {
        applyDecIntMathOperator(other, "-", Operators::sub, Operators::sub);
        return this;
    }

    public ConstantValue mul(ConstantValue other) {
        applyDecIntMathOperator(other, "*", Operators::mul, Operators::mul);
        return this;
    }

    public ConstantValue div(ConstantValue other) {
        applyDecIntMathOperator(other, "/", Operators::div, Operators::div);
        return this;
    }

    public ConstantValue mod(ConstantValue other) {
        applyIntMathOperator(other, "%", Operators::mod);
        return this;
    }

    public ConstantValue shiftLeft(ConstantValue other) {
        applyIntMathOperator(other, "<<", Operators::shiftLeft);
        return this;
    }

    public ConstantValue shiftRight(ConstantValue other) {
        applyIntMathOperator(other, ">>", Operators::shiftRight);
        return this;
    }

    public ConstantValue bitwiseAnd(ConstantValue other) {
        applyIntMathOperator(other, "&", Operators::and);
        return this;
    }

    public ConstantValue bitwiseXor(ConstantValue other) {
        applyIntMathOperator(other, "^", Operators::xor);
        return this;
    }

    public ConstantValue bitwiseOr(ConstantValue other) {
        applyIntMathOperator(other, "|", Operators::or);
        return this;
    }

    public ConstantValue equality(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " ==");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) == 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }
    public ConstantValue notEquals(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " !=");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) != 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }
    public ConstantValue lessThan(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " <");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) < 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }
    public ConstantValue lessThanOrEquals(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " <=");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) <= 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }
    public ConstantValue greaterThan(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " >");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) > 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }

    public ConstantValue greaterThanOrEquals(ConstantValue other) {
        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " >=");
        valueInt = getBigDecimalValue().compareTo(other.getBigDecimalValue()) >= 0 ? ONE.valueInt : ZERO.valueInt;
        valueDecimal = null;
        type = machine.primitives.INT;
        return this;
    }




    private void applyDecIntMathOperator(ConstantValue other, String operatorName, BiFunction<BigDecimal, BigDecimal, BigDecimal> decimalOperator, BiFunction<BigInteger, BigInteger, BigInteger> integerOperator) {
        if (other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + operatorName);
        if (other.valueDecimal != null || valueDecimal != null) {
            valueDecimal = decimalOperator.apply(getBigDecimalValue(), other.getBigDecimalValue());
            valueInt = null;
        }
        else { valueInt = integerOperator.apply(valueInt, other.valueInt); }
        type = dominantType(other.type, type);

        // TODO: infinity check
        // /, -, = NaN
        // 0.0 / 0.0 = -NaN??
        // TODO: seperate sign and isInfinite, isNaN
        // Infinity == infinity = true
        // inf < inf and inf > inf is false
        // but <= and >= is true

        if (other.isInfinite() || other.isNaN())
            this.valueDecimal = other.valueDecimal;

        this.isInfinite |= other.isInfinite();
        this.isNaN |= other.isNaN();
    }

    private void applyIntMathOperator(ConstantValue other, String operatorName, BiFunction<BigInteger, BigInteger, BigInteger> integerOperator) {
        if (other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + operatorName);
        if (other.valueDecimal != null || valueDecimal != null)
            throw new IllegalStateException("Cannot apply operator " + operatorName + " to decimal type");
        else { valueInt = integerOperator.apply(valueInt, other.valueInt); }
        type = dominantType(other.type, type);
    }

    private AbstractTypeStorage dominantType(AbstractTypeStorage type1, AbstractTypeStorage type2) {
        return TypeCasting.numericPromote((BaseTypeStorage)type1, (BaseTypeStorage)type2, machine);
    }

    public String toString() {
        if (valueDecimal != null) return valueDecimal.toString();
        if (valueInt != null) return valueInt.toString();
        if (valueString != null) return valueString;
        return "null";
    }

    public boolean equals(ConstantValue other) {
        // TODO object other

        if(other.valueString != null || valueString != null)
            throw new IllegalStateException("Invalid operands to binary " + " ==");
        return getBigDecimalValue().compareTo(other.getBigDecimalValue()) == 0;
    }

    /** Utilities for unifying BigInteger and BigDecimal operations */
    private static class Operators {
        public static BigDecimal add(BigDecimal a, BigDecimal b) { return a.add(b); }
        public static BigInteger add(BigInteger a, BigInteger b) { return a.add(b); }
        public static BigDecimal sub(BigDecimal a, BigDecimal b) { return a.subtract(b); }
        public static BigInteger sub(BigInteger a, BigInteger b) { return a.subtract(b); }
        public static BigDecimal mul(BigDecimal a, BigDecimal b) { return a.multiply(b); }
        public static BigInteger mul(BigInteger a, BigInteger b) { return a.multiply(b); }
        public static BigDecimal div(BigDecimal a, BigDecimal b) { return a.divide(b, RoundingMode.HALF_UP); }
        public static BigInteger div(BigInteger a, BigInteger b) { return a.divide(b); }

        public static BigInteger mod(BigInteger a, BigInteger b) { return a.mod(b); }
        public static BigInteger shiftLeft(BigInteger a, BigInteger b)  { return a.shiftLeft(b.intValue()); }
        public static BigInteger shiftRight(BigInteger a, BigInteger b) { return a.shiftRight(b.intValue()); }
        public static BigInteger and(BigInteger a, BigInteger b) { return a.and(b); }
        public static BigInteger or(BigInteger a, BigInteger b)  { return a.or(b); }
        public static BigInteger xor(BigInteger a, BigInteger b) { return a.xor(b); }
    }
}
