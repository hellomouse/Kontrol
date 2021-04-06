package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.FunctionParameter;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.storage_types.*;
import net.hellomouse.c_interp.compiler.Compiler;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

/**
 * Handle definitions of TypeName contexts, which describe only
 * the type and nothing else, for instance in sizeof or cast expressions
 * @author Bowserinator
 */
public class TypeNameHandler extends AbstractHandler {
    public TypeNameHandler(Compiler compiler) {super(compiler); }

    /**
     * Solve a type name context, returning an AbstractTypeStorage corresponding
     * to the described type.
     *
     * @param context Root node of the type name context
     * @param scope Scope to evaluate in
     * @return Type described
     */
    public AbstractTypeStorage handleTypeName(CParser.TypeNameContext context, AbstractScope scope) {
        AbstractTypeStorage type = null;

        for (int i = 0; i < context.getChildCount(); i++) {
            ParseTree child = context.getChild(i);
            if (child instanceof CParser.SpecifierQualifierListContext)
                type = compiler.definitionHandler.handleDeclarationSpecifier(child, scope);
            else if (child instanceof CParser.AbstractDeclaratorContext)
                type = solveAbstractDeclarator(child, type, scope);
        }
        return type;
    }

    /**
     * Solve an abstract declarator, which modifies a base type. Examples include:
     * <ul>
     *     <li>arrays (ie, int[5], the [5] is the abstract portion, the int is the base type)</li>
     *     <li>function parameter lists</li>
     *     <li>additional pointers</li>
     * </ul>
     *
     * @param tree Root node of the abstract declaration, should be an AbstractDeclaratorContext or an DirectAbstractDeclaratorContext
     * @param type Base type, result should be from the specifier qualifier list
     * @param scope Scope to evaluate in
     * @return New type modified with the abstract declarators
     */
    private AbstractTypeStorage solveAbstractDeclarator(ParseTree tree, AbstractTypeStorage type, AbstractScope scope) {
        int pointerLevel = 0;

        // Skip pointless intermediate nodes
        while (tree.getChildCount() == 1)
            tree = tree.getChild(0);

        // Increment pointer level
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i) instanceof CParser.PointerContext)
                pointerLevel++;
        }

        if (tree instanceof CParser.DirectAbstractDeclaratorContext) {
            // Function -> ()  OR   ( paramList )
            if (
                    (tree.getChildCount() == 2 && tree.getChild(1).getText().equals(")")) ||
                    (tree.getChildCount() == 3 && tree.getChild(2).getText().equals(")"))) {

                ArrayList<FunctionParameter> parameters = new ArrayList<>();
                compiler.definitionHandler.handleFunctionParameterList(tree, 1, parameters, scope);
                type = new FunctionTypeStorage(tree.getChild(0).getText(), type, parameters, true);
            }

            // Array
            else if (
                    (tree.getChildCount() == 2 && tree.getChild(1).getText().equals("]")) ||
                    (tree.getChildCount() == 3 && tree.getChild(2).getText().equals("]"))) {

                // Expected [??], [, AssignmentExpression, ]
                if (tree.getChildCount() == 3 && !(tree.getChild(1) instanceof CParser.AssignmentExpressionContext))
                    compiler.parserError("don't understand array size given", tree.getChild(2));

                if (tree.getChildCount() == 3) {
                    int length = ((ConstantValue)compiler.constantExprHandler.evalFileScopeExpr(tree.getChild(1))).getBigIntegerValue().intValue();
                    type = new ArrayTypeStorage(type, length);
                }
                else {
                    type = new IncompleteArrayTypeStorage(type);
                }
            }
        }

        // Cast to pointer type
        if (pointerLevel > 0)
            type = new PointerTypeStorage(type, pointerLevel);

        // Recursively solve children top-down
        // Note: there should only be 1 child below that can be solved
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof CParser.DirectAbstractDeclaratorContext)
                return solveAbstractDeclarator(child, type, scope);
        }

        return type;
    }
}
