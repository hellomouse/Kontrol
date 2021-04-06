package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.interfaces.ICompileTimeValue;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.ArrayValue;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.StructOrUnionValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.ArrayTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.interfaces.IArray;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;

/**
 * Evaluation of global scope expressions
 * @author Bowserinator
 */
public class FileScopeExprHandler extends AbstractHandler {
    /** Constructor */
    public FileScopeExprHandler(Compiler compiler) {super(compiler); }

    /**
     * Default constant expression evaluation, passes type as null. Will handle any expression
     * that is not an array or struct/union initialization
     *
     * @param expression Root node of the constant expression
     * @return Evaluated expression
     * @see FileScopeExprHandler#evalFileScopeExpr(ParseTree, AbstractTypeStorage)
     */
    public IExpression evalFileScopeExpr(ParseTree expression) {
        return evalFileScopeExpr(expression, null);
    }

    /**
     * Evaluate a compile time constant expression. Limited support is offered for decoding
     * enums and memory addresses of known compile time variables. Output expression must be an
     * int type (char, short, int, etc...), floating type (float, double, long double) or a C
     * string constant
     *
     * @param expression Root node of the constant expression
     * @param type Type of the left hand, only necessary if initializing an array or struct/union
     * @return Evaluated expression
     */
    public IExpression evalFileScopeExpr(ParseTree expression, AbstractTypeStorage type) {
        // Struct or union initializer, { initializerList }
        if (expression instanceof CParser.InitializerContext && expression.getChild(0) instanceof TerminalNodeImpl)
            return structOrUnionOrArrayDefinition(expression.getChild(1), type);

        // Delegate rest to expression handler, flagging as global scope only
        return compiler.expressionHandler.handleExpression(expression, compiler.globalScope, true);
    }

    /**
     * Expression for an initializer list such as {1,2,3} or {.x=1}, which are used to initialize structs/unions
     * and arrays.
     *
     * @param expression Root node of expression
     * @param type Type the expression is being assigned to, must be struct/union or array or incomplete array
     * @return Compile time value representing the initialized type
     */
    private ICompileTimeValue structOrUnionOrArrayDefinition(ParseTree expression, AbstractTypeStorage type) {
        // Only assign { ... } to struct, union or array
        // Incomplete array types are allowed because size is set during this definition stage
        // Incomplete struct / union types are NOT allowed, they will error before this is called
        if (!(type instanceof StructOrUnionStorage) && !(type instanceof IArray)) {
            compiler.parserError("array or struct/union definition, but is not a struct, union, or array type", expression);
            return null;
        }

        int fieldIndex = 0;
        boolean isStructOrUnion = type instanceof StructOrUnionStorage;

        StructOrUnionStorage structOrUnionType = isStructOrUnion ?
                (StructOrUnionStorage)type : null;

        // initializer -> { initializerList }
        // initializerList -> initializerList, initializer

        // designation -> designatorList, =

        ArrayList<ParseTree> children = CompilerUtil.getTreesByRule(expression,
                ctx -> !(ctx instanceof CParser.InitializerListContext) && !(ctx instanceof CParser.DesignationContext), true);

        ArrayList<String> fields = new ArrayList<>();
        ArrayList<IExpression> values = new ArrayList<>();

        for (ParseTree tree : children) {
            // Assigning a field, ie { .x = 1 }, get index
            if (tree instanceof CParser.DesignatorListContext && isStructOrUnion) {
                // DesignatorList -> Designator -> ., name
                String field = tree.getChild(0).getChild(1).getText();
                fieldIndex = structOrUnionType.valueIndexMap.getOrDefault(field, -1);

                if (fieldIndex < 0)
                    compiler.error("unknown field '" + field + "' specified in initializer", tree);
            }

            // Handle assignment
            else if (tree instanceof CParser.InitializerContext) {
                IExpression value;

                if (isStructOrUnion) {
                    if (fieldIndex >= structOrUnionType.fields.size()) {
                        compiler.warning("excess elements in " + (structOrUnionType.isStruct ? "struct" : "union") + " initializer", expression);
                        continue;
                    }

                    String designatorKey = structOrUnionType.fields.get(fieldIndex).name;
                    fields.add(designatorKey);
                }

                // { initializerList }, a recursive definition
                if (tree.getChildCount() == 3 && tree.getChild(0) instanceof TerminalNodeImpl && tree.getChild(1) instanceof CParser.InitializerListContext) {
                    AbstractTypeStorage newType = isStructOrUnion ?
                                structOrUnionType.fields.get(fieldIndex).type :
                                ((IArray)type).getElementType();
                    value = structOrUnionOrArrayDefinition(tree.getChild(1), newType);
                }
                // Directly defined value
                else { value = evalFileScopeExpr(tree); }

                values.add(value);
                fieldIndex++;
            }
        }

        // Return new struct type
        if (isStructOrUnion) {
            StructOrUnionValue returned = new StructOrUnionValue(structOrUnionType);
            for (int i = 0; i < values.size(); i++)
                returned.addValue(fields.get(i), values.get(i));
            return returned;
        }

        // Return new array type
        ArrayTypeStorage arrType = type instanceof ArrayTypeStorage ?
            (ArrayTypeStorage)type :   // Predefined size
            new ArrayTypeStorage(((IArray)type).getElementType(), values.size()); // Incomplete type

        if (values.size() > arrType.length)
            compiler.warning("excess elements in array initializer", expression);

        // Extra elements are initialized to 0
        if (values.size() < arrType.length) {
            for (int i = 0; i <= arrType.length - values.size(); i++)
                values.add(new ConstantValue(compiler.machine, "0"));
        }

        return new ArrayValue(arrType, values);
    }
}
