package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.FunctionCallExpression;
import net.hellomouse.c_interp.common.expressions.StructOrUnionMemberExpression;
import net.hellomouse.c_interp.common.expressions.VariableExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.operations.operations.*;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.expressions.types.TypeCheck;
import net.hellomouse.c_interp.common.expressions.util.Literals;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.scope.GlobalScope;
import net.hellomouse.c_interp.common.specifiers.TypeQualifier;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.ArrayTypeStorage;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.interfaces.IIncompleteType;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

/**
 * Handles all expressions, such as a + 1, func(), etc...
 * @author Bowserinator
 */
public class ExpressionHandler extends AbstractHandler {
    public ExpressionHandler(Compiler compiler) { super(compiler); }

    /**
     * Handles expression in local scope (allowing all operations)
     * @param expression Root node of the expression, can contain children that are also expressions, but
     *                   ALL children must be an expression-like context
     * @param scope Scope the expression is evaluated in
     * @return Parsed expression instance
     * @see ExpressionHandler#handleExpression(ParseTree, AbstractScope, boolean)
     */
    public IExpression handleExpression(ParseTree expression, AbstractScope scope) {
        return handleExpression(expression, scope, false);
    }

    /**
     * <p>Parse an expression, returning an IExpression instance. This can be a constant value,
     * a variable name, or an Operation on IExpression operands. The expression is not evaluated
     * at compile time, although there may be simple checks for types and such.</p>
     *
     * <p>Known compile time expressions or subexpressions, such as int x = 1 + 1 will be evaluated, in this example it
     * will produce an expression x = 2</p>
     *
     * @param expression Root node of the expression, can contain children that are also expressions, but
     *                   ALL children must be an expression-like context
     * @param scope Scope the expression is evaluated in
     * @param treatAsGlobalScopeExpression Only allow operations that are valid in the global scope
     *                                     This is a separate parameter from scope to allow extra operations where it's legal
     *                                     such as within a sizeof(), while keeping the same global scope
     * @return Parsed expression instance
     */
    public IExpression handleExpression(ParseTree expression, AbstractScope scope, boolean treatAsGlobalScopeExpression) {
        // Lots of intermediate nodes that don't do anything, so we traverse directly down
        while (expression.getChildCount() == 1) {
            expression = expression.getChild(0);

            // Ignore parentheses around expression, solve the expression in the parentheses instead
            // ( expression ) -> expression
            while (expression instanceof CParser.PrimaryExpressionContext &&
                    expression.getChildCount() == 3 &&
                    expression.getChild(0).getText().equals("(") &&
                    expression.getChild(2).getText().equals(")"))
                expression = expression.getChild(1);
        }

        // Terminal node, try to evaluate it directly
        if (expression.getChildCount() == 0)
            return tryAndGetValue(expression, scope);

        // Concat adjacent strings
        if (expression instanceof CParser.PrimaryExpressionContext) {
            StringBuilder concatStr = new StringBuilder();
            boolean valid = true;

            for (int i = 0; i < expression.getChildCount(); i++) {
                String val = expression.getChild(i).getText();
                String temp_str = Literals.processStringLiteral(val);

                if (temp_str == null) {
                    valid = false;
                    break;
                }
                concatStr.append(temp_str);
            }
            if (valid)
                return new ConstantValue(compiler.machine, "\"" + concatStr.toString() + "\"");
        }

        // Type cast
        if (expression instanceof CParser.CastExpressionContext) {
            // ( typeName ) castExpression
            if (expression.getChildCount() != 4 || !(expression.getChild(1) instanceof CParser.TypeNameContext) || !(expression.getChild(3) instanceof CParser.CastExpressionContext))
                compiler.parserError("malformed cast expression", expression);

            // typeName
            CParser.TypeNameContext typeNameContext = (CParser.TypeNameContext)expression.getChild(1).getChild(0);
            AbstractTypeStorage type = compiler.typeNameHandler.handleTypeName(typeNameContext, scope);

            if (!type.isScalar())
                compiler.error("conversion to non-scalar type requested", typeNameContext);
            if (type instanceof ArrayTypeStorage)
                compiler.error("cast specifies array type", typeNameContext);

            IExpression operand = handleExpression(expression.getChild(3), scope, treatAsGlobalScopeExpression);
            return new TypeCastOperation(type, operand, compiler.machine);
        }

        // Sizeof and variants
        if (expression.getChild(0).getText().equals("sizeof")) {
            // Sizeof a type, ie sizeof(int)
            if (expression.getChild(1).getText().equals("(") && expression.getChild(2) instanceof CParser.TypeNameContext)
                return getSizeofType(expression.getChild(2), scope);

            // Sizeof an expression, ie sizeof(1+1)
            else if (expression.getChild(1) instanceof CParser.UnaryExpressionContext) {
                // UnaryExpression
                //  - sizeof
                //  - unaryExpression / postFixExpression / primaryExpression
                //      -  (  expression  )

                ParseTree expr = expression.getChild(1);
                while (!(expr.getChild(0).getText().equals("(")))
                    expr = expr.getChild(0);
                expr = expr.getChild(1);

                return getSizeofType(expr, scope);
            }

            compiler.parserError("could not parse sizeof expression", expression);
            return null;
        }

        if (expression instanceof CParser.PostfixExpressionContext) {
            // Function call, funcName ( ... )
            if (expression.getChild(0) instanceof CParser.PostfixExpressionContext &&
                    expression.getChildCount() > 2 &&
                    expression.getChild(expression.getChildCount() - 1).getText().equals(")") &&
                    expression.getChild(1).getText().equals("(")) {

                notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                ArrayList<IExpression> arguments = new ArrayList<>();

                // postFixExpression, (, argumentExpressionList, )
                //                         - assignmentExpression...
                if (expression.getChildCount() == 4 && expression.getChild(3).getText().equals(")")) {
                    ArrayList<ParseTree> expressions = CompilerUtil.getTreesByRule(expression.getChild(2), ctx -> ctx instanceof CParser.AssignmentExpressionContext, true);
                    for (ParseTree expr : expressions)
                        arguments.add(handleExpression(expr, scope, treatAsGlobalScopeExpression));
                }
                // postFixExpression, (, )
                // If has arguments, but doesn't match format above
                else if (!(expression.getChildCount() == 3 && expression.getChild(2).getText().equals(")"))) {
                    compiler.parserError("cannot parse expression as function call", expression);
                    return null;
                }

                String funcName = expression.getChild(0).getText();
                FunctionTypeStorage funcType = scope.getFunction(funcName);

                // Function doesn't exist
                if (funcType == null) {
                    compiler.error("undefined reference to '" + funcName + "'", expression);
                    return null;
                }

                funcType.validiateArguments(arguments, compiler, expression);
                return new FunctionCallExpression(funcName, arguments, funcType);
            }

            // Regular postfix expression
            IExpression val1 = handleExpression(expression.getChild(0), scope, treatAsGlobalScopeExpression);

            switch (expression.getChild(1).getText()) {
                case "++" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericOrPointerUnary(compiler, expression.getChild(1), val1.getType(), "increment");
                    return new IncDecOp(IncDecOp.IncDecOpEnum.POST_INC, val1, compiler.machine);
                }
                case "--" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericOrPointerUnary(compiler, expression.getChild(1), val1.getType(), "decrement");
                    return new IncDecOp(IncDecOp.IncDecOpEnum.POST_DEC, val1, compiler.machine);
                }
                case "->" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    StructOrUnionMemberExpression val2 = handleStructOrUnionMember(val1, expression.getChild(2));
                    return new MiscBinOp(MiscBinOp.BinOp.DEREFERENCE, val1, val2, compiler.machine);
                }
                case "." -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    StructOrUnionMemberExpression val2 = handleStructOrUnionMember(val1, expression.getChild(2));
                    return new MiscBinOp(MiscBinOp.BinOp.REFERENCE, val1, val2, compiler.machine);
                }
                case "[" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    IExpression val2 = handleExpression(expression.getChild(2), scope, treatAsGlobalScopeExpression);
                    return new MiscBinOp(MiscBinOp.BinOp.SUBSCRIPT, val1, val2, compiler.machine);
                }
            }

            // Should never run as ANTLR4 would error first
            compiler.parserError("unknown postfix operator: " + expression.getChild(1).getText(), expression);
            return null;
        }
        else if (expression instanceof CParser.UnaryExpressionContext) {
            // [OP] [EXPR]
            IExpression val1 = handleExpression(expression.getChild(1), scope, treatAsGlobalScopeExpression);

            switch (expression.getChild(0).getText()) {
                case "!" -> {
                    TypeCheck.checkScalarUnary(compiler, expression.getChild(0), val1.getType(), "unary exclamation mark");
                    if (val1 instanceof ConstantValue)
                        return ((ConstantValue) val1).not();
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.NOT, val1, compiler.machine);
                }
                case "-" -> {
                    TypeCheck.checkNumericUnary(compiler, expression.getChild(0), val1.getType(), "unary minus");
                    if (val1 instanceof ConstantValue)
                        return ((ConstantValue)val1).neg();
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.NEG, val1, compiler.machine);
                }
                case "+" -> {
                    TypeCheck.checkNumericUnary(compiler, expression.getChild(0), val1.getType(), "unary plus");
                    if (val1 instanceof ConstantValue)
                        return ((ConstantValue)val1).pos();
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.POS, val1, compiler.machine);
                }
                case "~" -> {
                    TypeCheck.checkIntegerUnary(compiler, expression.getChild(0), val1.getType(), "bit-complement");
                    if (val1 instanceof ConstantValue)
                        return ((ConstantValue)val1).bitwiseNot();
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.BITNOT, val1, compiler.machine);
                }
                case "&"  -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.ADDRESS, val1, compiler.machine);
                }
                case "++" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericOrPointerUnary(compiler, expression.getChild(0), val1.getType(), "increment");
                    return new IncDecOp(IncDecOp.IncDecOpEnum.PRE_INC, val1, compiler.machine);
                }
                case "--" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericOrPointerUnary(compiler, expression.getChild(0), val1.getType(), "decrement");
                    return new IncDecOp(IncDecOp.IncDecOpEnum.PRE_DEC, val1, compiler.machine);
                }
                case "*"  -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkPointerIndirection(compiler, expression.getChild(0), val1.getType());
                    return new MiscPrefixOp(MiscPrefixOp.PrefixOp.INDIRECTION, val1, compiler.machine);
                }
            }

            // Should never run as ANTLR4 would error first
            compiler.parserError("unknown prefix operator: " + expression.getChild(0).getText(), expression);
            return null;
        }


        // -----------------------------------------
        // IMPORTANT
        // All operators after this point should be in the form EXPR OPERATOR EXPR ...
        // or an error may be thrown when evaluating the incorrect node as operator
        // This is also why all the unary operators are above
        // ------------------------------------------

        IExpression val1 = handleExpression(expression.getChild(0), scope, treatAsGlobalScopeExpression);
        IExpression val2 = handleExpression(expression.getChild(2), scope, treatAsGlobalScopeExpression);
        String opSymbol = expression.getChild(1).getText();

        // Assignment, [expr] = [expr]
        if (expression instanceof CParser.AssignmentExpressionContext) {
            String operator = expression.getChild(1).getText();
            AbstractTypeStorage type = val1.getType();

            // Assignment of const only
            if (val1 instanceof VariableExpression && (type.getTypeQualifiers() & TypeQualifier.CONST) != 0) {
                String varName = ((VariableExpression)val1).variableName;
                compiler.error("assignment of read-only variable '" + varName + "'", expression);
            }

            // General assignment check
            TypeCheck.checkAssignment(compiler, expression.getChild(2), val1.getType(), val2.getType());

            // Restrictions for specific assignment
            switch(operator) {
                case "+=", "-=" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericOrPointerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), operator);
                }
                case "/=", "*=" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkNumericBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), operator);
                }
                case "%=", "^=", "|=", "&=", "<<=", ">>=" -> {
                    notConstantCheck(expression.getChild(0), treatAsGlobalScopeExpression);
                    TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), operator);
                }
            }
            return new AssignmentOp(AssignmentOp.getOperatorBySymbol(operator), val1, val2, compiler.machine);
        }

        // +, -, *, /, %, <<, >>
        if (expression instanceof CParser.AdditiveExpressionContext) {
            TypeCheck.checkNumericOrPointerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            switch (opSymbol) {
                case "+" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).add((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.ADD, val1, val2, compiler.machine);
                }
                case "-" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).sub((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.SUB, val1, val2, compiler.machine);
                }
            }
        }
        if (expression instanceof CParser.MultiplicativeExpressionContext) {
            // % operator only works with ints
            if (opSymbol.equals("%")) { TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol); }
            else                      { TypeCheck.checkNumericBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol); }

            switch (opSymbol) {
                case "*" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).mul((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.MUL, val1, val2, compiler.machine);
                }
                case "/" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).div((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.DIV, val1, val2, compiler.machine);
                }
                case "%" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).mod((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.MOD, val1, val2, compiler.machine);
                }
            }
        }
        if (expression instanceof CParser.ShiftExpressionContext) {
            TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            switch (opSymbol) {
                case "<<" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).shiftLeft((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.LSHIFT, val1, val2, compiler.machine);
                }
                case ">>" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).shiftRight((ConstantValue) val2);
                    return new BinMathOp(BinMathOp.MathOp.RSHIFT, val1, val2, compiler.machine);
                }
            }
        }

        // Bitwise Logic: &, |, ^
        if (expression instanceof CParser.AndExpressionContext) {
            TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                return ((ConstantValue) val1).bitwiseAnd((ConstantValue) val2);
            return new BinLogicOp(BinLogicOp.LogicOp.BITAND, val1, val2, compiler.machine);
        }
        if (expression instanceof CParser.ExclusiveOrExpressionContext) {
            TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                return ((ConstantValue) val1).bitwiseXor((ConstantValue) val2);
            return new BinLogicOp(BinLogicOp.LogicOp.BITXOR, val1, val2, compiler.machine);
        }
        if (expression instanceof CParser.InclusiveOrExpressionContext) {
            TypeCheck.checkIntegerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                return ((ConstantValue) val1).bitwiseOr((ConstantValue) val2);
            return new BinLogicOp(BinLogicOp.LogicOp.BITOR, val1, val2, compiler.machine);
        }

        // Conditional expressions
        // ConditionalExpression -> { expression, ?, expression, :, expression }
        if (expression instanceof CParser.ConditionalExpressionContext) {
            IExpression val3 = handleExpression(expression.getChild(4), scope, treatAsGlobalScopeExpression);
            TypeCheck.checkTernary(compiler, expression.getChild(1), expression.getChild(3), val1.getType(), val2.getType(), val3.getType());

            if (val1 instanceof ConstantValue)
                return ((ConstantValue) val1).isNotZero() ? val2 : val3;
            return new TernaryOp(val1, val2, val3, compiler.machine);
        }

        // &&
        if (expression instanceof CParser.LogicalAndExpressionContext) {
            TypeCheck.checkScalarBinaryIndividual(compiler, expression.getChild(0), expression.getChild(1), val1.getType(), val2.getType());
            if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                return ((ConstantValue) val1).and((ConstantValue) val2);
            return new BinLogicOp(BinLogicOp.LogicOp.AND, val1, val2, compiler.machine);
        }
        // ||
        if (expression instanceof CParser.LogicalOrExpressionContext) {
            TypeCheck.checkScalarBinaryIndividual(compiler, expression.getChild(0), expression.getChild(1), val1.getType(), val2.getType());
            if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                return ((ConstantValue) val1).or((ConstantValue) val2);
            return new BinLogicOp(BinLogicOp.LogicOp.OR, val1, val2, compiler.machine);
        }

        // Relational expressions, ie <, >
        if (expression instanceof CParser.RelationalExpressionContext) {
            TypeCheck.checkNumericOrPointerBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            switch (opSymbol) {
                case "<" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).lessThan((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.LT, val1, val2, compiler.machine);
                }
                case ">" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).greaterThan((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.GT, val1, val2, compiler.machine);
                }
                case "<=" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).lessThanOrEquals((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.LT_EQ, val1, val2, compiler.machine);
                }
                case ">=" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).greaterThanOrEquals((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.GT_EQ, val1, val2, compiler.machine);
                }
            }
        }

        // Equality expressions, ie ==
        if (expression instanceof CParser.EqualityExpressionContext) {
            TypeCheck.checkScalarBinary(compiler, expression.getChild(1), val1.getType(), val2.getType(), opSymbol);
            switch (opSymbol) {
                case "==" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).equality((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.EQUAL, val1, val2, compiler.machine);
                }
                case "!=" -> {
                    if (val1 instanceof ConstantValue && val2 instanceof ConstantValue)
                        return ((ConstantValue) val1).notEquals((ConstantValue) val2);
                    return new ComparisonOp(ComparisonOp.CompareOp.NOT_EQUAL, val1, val2, compiler.machine);
                }
            }
        }

        if (expression.getChildCount() == 3 && expression.getChild(1).getText().equals(","))
            return new MiscBinOp(MiscBinOp.BinOp.COMMA, val1, val2, compiler.machine);

        return tryAndGetValue(expression, scope);
    }

    /**
     * Attempt to parse text as an IExpression, returning either a CompileTimeValue or RuntimeVariable
     * @param expr Parse tree with text to parse, can be a constant (ie "1", "1.0e65", "'h'")
     *             Or containing a valid compile time enum name, or a valid variable name in the scope
     * @return IExpression
     */
    private IExpression tryAndGetValue(ParseTree expr, AbstractScope scope) {
        String text = expr.getText();

        // Check if expr is valid variable
        Pair<Variable, AbstractScope> localVar = scope.getVariableAndScope(text);
        if (localVar != null)
            return new VariableExpression(text, localVar.a, localVar.b.getId());

        // Evaluate expr as constant
        ConstantValue value = null;

        try {
            // .getOrDefault evaluates default expression so we cannot use it
            ConstantValue enumVal = scope.getEnumValue(text);
            value = enumVal != null ? enumVal : new ConstantValue(compiler.machine, text);
        }
        catch(Exception e) {
            compiler.error("'" + text + "' undeclared " +
                    (scope instanceof GlobalScope ?
                            "here (not in a function)" :
                            "(first use in this function") +
                    ")", expr);
        }
        return value;
    }

    /**
     * Return a struct or union member expression, used for handling accessing struct/union members
     * for operations such as myStructPointer->x or myStruct.x
     *
     * @param structOrUnionExpression Expression for the variable (ie, in x.y, 'x' is this expression) that
     *                                returns a type of StructOrUnionStorage
     * @param memberNameContext Context of the member, should contain only the member name
     * @return Struct or union member expression
     */
    private StructOrUnionMemberExpression handleStructOrUnionMember(IExpression structOrUnionExpression, ParseTree memberNameContext) {
        // Base is not a struct
        if (!(structOrUnionExpression instanceof VariableExpression) && !(structOrUnionExpression.getType() instanceof StructOrUnionStorage)) {
            compiler.parserError("base type is not a struct or union", memberNameContext);
            return null;
        }

        StructOrUnionStorage type = (StructOrUnionStorage)structOrUnionExpression.getType();
        String memberName = memberNameContext.getText();

        // Name doesn't exist
        if (!type.valueIndexMap.containsKey(memberName)) {
            compiler.error("'" + type.getFullDeclaration() + "' has no member named '" + memberName + "'", memberNameContext);
            return null;
        }

        return new StructOrUnionMemberExpression(type.fields.get(type.valueIndexMap.get(memberName)).type, memberName);
    }

    /**
     * Returns the size of the type of an expression in bytes as an integer constant value
     * @param node Node pointing to either an expression or a TypeNameContext
     * @param scope Scope to evaluate in
     * @return Integer constant value representing size of the type in bytes
     */
    private ConstantValue getSizeofType(ParseTree node, AbstractScope scope) {
        AbstractTypeStorage type = (node instanceof CParser.TypeNameContext) ?
                compiler.typeNameHandler.handleTypeName((CParser.TypeNameContext)node, scope) :
                handleExpression(node, scope, false).getType();

        // Cannot get size of incomplete type
        if (type instanceof IIncompleteType) {
            if (type instanceof INamedType) {
                compiler.error("invalid application of 'sizeof' to incomplete type '" + ((INamedType) type).getFullDeclaration() + "'", node);
                return null;
            }
            compiler.error("invalid application of 'sizeof' to incomplete type", node);
            return null;
        }
        return new ConstantValue(compiler.machine, type.getSize());
    }

    /**
     * Simple check to error if currently in the global scope. Run before operations that are not
     * valid in the global scope.
     *
     * @param context Context to point to for the error
     * @param treatAsGlobalScopeExpression Treating expression as being parsed in the global scope?
     */
    private void notConstantCheck(ParseTree context, boolean treatAsGlobalScopeExpression) {
        if (treatAsGlobalScopeExpression)
            compiler.error("initializer element is not constant", context);
    }
}
