package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.EnumStorage;
import net.hellomouse.c_interp.common.storage_types.InvalidTypeStorage;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Handle definitions of enums
 * @author Bowserinator
 */
public class EnumHandler extends AbstractHandler {
    public EnumHandler(Compiler compiler) {super(compiler); }

    /**
     * Construct an enum.
     * <ul>
     *     <li>If the enum definition exists and is being re-used, ie enum myEnum myVar;
     *     it will return the existing enum</li>
     *     <li>If the enum defines enumerators, it will return a new type and add the type names and such
     *          to the compiler</li>
     * </ul>
     *
     * @param context Root node of the enum specifier
     * @param scope Scope to evaluate in
     * @return EnumStorage, or InvalidTypeStorage if the enum is invalid
     */
    public AbstractTypeStorage enumFromEnumSpecifierContext(CParser.EnumSpecifierContext context, AbstractScope scope) {
        String name = null;
        ParseTree enumeratorStart = null;

        // Detect empty enums
        if (context.getChildCount() == 3)
            compiler.error("empty enum is invalid", context);
        else if (context.getChildCount() == 4 && context.getChild(2) instanceof TerminalNodeImpl)
            compiler.error("empty enum is invalid", context);

        // Verify top level structure matches 'enum', name, {, enumeratorList, }
        // Or 'enum', {, enumeratorList, }

        // enum { list }
        if (context.getChildCount() == 4 && context.getChild(1) instanceof TerminalNodeImpl && context.getChild(2) instanceof CParser.EnumeratorListContext && context.getChild(3) instanceof TerminalNodeImpl) {
            name = "";
            enumeratorStart = context.getChild(2);
        }
        // enum name { list }
        else if (context.getChildCount() == 5 && context.getChild(2) instanceof TerminalNodeImpl && context.getChild(3) instanceof CParser.EnumeratorListContext && context.getChild(4) instanceof TerminalNodeImpl) {
            name = context.getChild(1).getText();
            enumeratorStart = context.getChild(3);
        }
        // enum name
        else if (context.getChildCount() == 2 && context.getChild(1) instanceof TerminalNodeImpl) {
            name = context.getChild(1).getText();

            EnumStorage type = scope.getEnumType(name);
            return type == null ? new InvalidTypeStorage() : type;
        }

        // Should never happen
        if (name == null || enumeratorStart == null) {
            compiler.parserError("unable to parse expression as enum", context);
            return new InvalidTypeStorage();
        }

        // Enum name redefinition
        if (!name.isEmpty() && scope.customEnumTypes.containsKey(name))
            compiler.error("redefinition of 'enum " + name + "'", context.getChild(1));

        // Load all the values
        BigInteger currentValue = BigInteger.valueOf(0);
        ArrayList<EnumStorage.EnumValue> values = new ArrayList<>();
        ArrayList<ParseTree> expressions = CompilerUtil.getTreesByRule(enumeratorStart, ctx -> ctx instanceof CParser.EnumeratorContext, true);

        for (ParseTree tree : expressions) {
            String valueName = tree.getChild(0).getText();

            // Assigned a value, ie A = 1
            if (tree.getChildCount() == 3 && tree.getChild(2) instanceof CParser.ConstantExpressionContext) {
                IExpression val = compiler.constantExprHandler.evalFileScopeExpr(tree.getChild(2));
                if (!(val instanceof ConstantValue) || !((ConstantValue) val).isInt()) {
                    compiler.error("enumerator value for '" + valueName + "' is not an integer constant", tree.getChild(2));
                    return null;
                }
                currentValue = ((ConstantValue)val).getBigIntegerValue();
            }

            if (scope.enumValues.containsKey(valueName))
                compiler.error("redeclaration of enumerator '" + valueName + "'", tree);

            scope.enumValues.put(valueName, new ConstantValue(compiler.machine, currentValue));

            values.add(new EnumStorage.EnumValue(valueName, currentValue));
            currentValue = currentValue.add(BigInteger.valueOf(1));
        }
        return new EnumStorage(name, values);
    }
}
