package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.exceptions.InvalidSpecifierException;
import net.hellomouse.c_interp.common.expressions.interfaces.ICompileTimeValue;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.*;
import net.hellomouse.c_interp.common.expressions.types.TypeCheck;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.scope.GlobalScope;
import net.hellomouse.c_interp.common.specifiers.FunctionSpecifier;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.common.storage_types.*;
import net.hellomouse.c_interp.common.storage_types.interfaces.IIncompleteType;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.*;

/**
 * Handle declarations and definitions of variables
 * @author Bowserinator
 */
public class DefinitionHandler extends AbstractHandler {
    /** Constructor */
    public DefinitionHandler(Compiler compiler) {super(compiler); }

    /**
     * Returns an array of variables in an declaration. Will handle variable names and
     * types. Typedefs are also handled as variables, check storageSpecifiers of the variable
     * to see if it defines a type.
     *
     * @param context Declaration context to parse
     * @param scope Scope to evaluate in
     * @return Array of declared variables
     */
    public ArrayList<Variable> handleDeclaration(ParseTree context, AbstractScope scope) {
        if (!(context.getChild(0) instanceof CParser.DeclarationSpecifiersContext))
            throw new IllegalStateException("Expected declaration specifier idk");

        AbstractTypeStorage T = handleDeclarationSpecifier(context.getChild(0), scope);

        if (context.getChild(1) instanceof CParser.InitDeclaratorListContext)
            return processInitDeclaratorContext((CParser.InitDeclaratorListContext)context.getChild(1), T, scope);

        // 2nd child is not of initDeclaratorList, this can happen when an enum is declared, ie
        // enum { A, B };
        // This can be safely ignored; we pass an empty array of declarations so the enum can be processed
        // even though no variables are declared
        return new ArrayList<>();
    }

    /**
     * <p>Handle the right side of the declaration, which contains the variable names and modifiers such
     * as pointers, functions, and array definitions. Also solves any variable initializers using
     * solveDeclarator</p>
     *
     * <p>For example, in int x[5],
     * <ul>
     *     <li>the 'int' is the base type and is solved in handleDeclaratorSpecifier</li>
     *     <li>the x[5] portion is solved here</li>
     * </ul></p>
     *
     * @param context Root node of the init declarator list
     * @param T Base type, solved from handleDeclarationSpecifier
     * @param scope Scope to evaluate in
     * @return Array of declared variables
     * @see DefinitionHandler#handleDeclarationSpecifier(ArrayList, AbstractScope)
     * @see DefinitionHandler#solveDeclarator(ParseTree, AbstractTypeStorage, AbstractScope)
     */
    private ArrayList<Variable> processInitDeclaratorContext(CParser.InitDeclaratorListContext context, AbstractTypeStorage T, AbstractScope scope) {
        // from initDeclarator list -> initDeclarator
        // -> declarator, or declarator = initializer
        //      -> declarator
        //          -> pointers
        //          -> directDeclarator
        //            -> directDeclarator -> directDeclarator ( )
        //            -> directDeclarator -> directDeclarator [ expr ]
        //            -> directDeclarator -> directDeclarator ( paramList )

        boolean isGlobal = scope instanceof GlobalScope;

        ArrayList<ParseTree> variables = CompilerUtil.getTreesByRule(context, ctx -> ctx instanceof CParser.InitDeclaratorContext, true);
        ArrayList<Variable> definitions = new ArrayList<>();

        for (ParseTree variable : variables) {
            IExpression initValue = null;
            AbstractTypeStorage type = T;

            // Get first child that's a declarator and recursively solve the type
            NameAndType result = solveDeclarator(variable.getChild(0), type, scope);
            String name = result.name;
            type = result.type;

            // [declarator] = [initializer]
            if (variable.getChildCount() == 3 && variable.getChild(1) instanceof TerminalNodeImpl && variable.getChild(2) instanceof CParser.InitializerContext) {
                // Incomplete arrays can become complete if declared with the type
                if (!(type instanceof IncompleteArrayTypeStorage)) {
                    if (type instanceof InvalidTypeStorage)
                        compiler.error("variable '" + name + "' has initializer but incomplete type", variable, false);
                    else if (type instanceof IIncompleteType)
                        compiler.error("variable '" + name + "' has initializer but incomplete type", variable, false);
                }

                initValue = isGlobal ?
                        compiler.constantExprHandler.evalFileScopeExpr(variable.getChild(2), type) :
                        compiler.expressionHandler.handleExpression(variable.getChild(2), scope);
                if (type instanceof IncompleteArrayTypeStorage)
                    type = new ArrayTypeStorage(((IncompleteArrayTypeStorage) type).elementType, ((ArrayValue)initValue).values.size());
            }

            // Same checks as above, different error message
            if (type instanceof InvalidTypeStorage)
                compiler.error("storage size of '" + name + "' isn’t known", variable);
            if (type.equals(compiler.machine.primitives.VOID))
                compiler.error("variable or field '" + name + "' declared void", variable);

            if (isGlobal) {
                // C functions can only be defined globally
                // (Compiler may offer an extension for locally defining functions, however)
                if (type instanceof FunctionTypeStorage) {
                    // Function prototype re-definition check
                    FunctionTypeStorage existingFunction = scope.getFunction(name);
                    if (existingFunction != null && !type.equals(existingFunction))
                        compiler.error("conflicting types for '" + name + "'", context);

                    compiler.globalScope.functionDeclarations.put(name, (FunctionTypeStorage) type);
                }

                else if (type instanceof IIncompleteType && type instanceof INamedType) {
                    String typeName = ((INamedType)type).getName();
                    compiler.globalScope.incompleteVariables.computeIfAbsent(typeName, k -> new ArrayList<>());
                    compiler.globalScope.incompleteVariables.get(typeName).add(name);
                }
            }

            // Variable redefinition check
            if (scope.variables.containsKey(name)) {
                if (scope.variables.get(name) instanceof PassedParameter)
                    compiler.error("'" + name + "' redeclared as different kind of symbol", context);
                else
                    compiler.error("redefinition of '" + name + "'", context);
            }

            boolean isTypedef = (type.getStorageSpecifiers() & StorageSpecifier.TYPEDEF) != 0;

            // Function specifier check
            if (!(type instanceof FunctionTypeStorage) && type.getFunctionSpecifiers() != 0) {
                for (int functionSpecifier : FunctionSpecifier.ALL_SPECIFIERS) {
                    if ((functionSpecifier & type.getFunctionSpecifiers()) != 0)
                        compiler.warning("variable '" + name + "' declared '" + FunctionSpecifier.specifierToString(functionSpecifier) + "'", context);
                }
            }

            // Type check
            if (!isTypedef && initValue != null)
                TypeCheck.checkInit(compiler, context, initValue.getType(), type);

            // Typedef
            if (isTypedef) {
                type.clearTypedef();

                // Check for conflicting type definitions
                AbstractTypeStorage typedef = scope.getTypedef(name);
                if (typedef != null && !typedef.equals(type))
                    compiler.error("conflicting types for '" + name + "'", context);

                scope.typedefTypes.put(name, type);
            }
            // Compile time constant
            else if (initValue instanceof ICompileTimeValue)
                scope.variables.put(name, new Variable(name, type, initValue));
            // Runtime expression
            else if (!(type instanceof FunctionTypeStorage))
                scope.variables.put(name, new Variable(name, type, initValue));

            definitions.add(new Variable(name, type, initValue));
        }

        return definitions;
    }

    /**
     * Run handleDeclarationSpecifier with all direct children of the context, useful for
     * CParser.DeclarationSpecifiersContext
     *
     * @param context Context with direct children valid for handleDeclarationSpecifier
     * @param scope Scope to evaluate in
     * @return type
     * @see DefinitionHandler#handleDeclarationSpecifier(ArrayList, AbstractScope)
     */
    public AbstractTypeStorage handleDeclarationSpecifier(ParseTree context, AbstractScope scope) {
        return handleDeclarationSpecifier(CompilerUtil.getTreesByRule(context, ctx -> ctx != context, true), scope);
    }

    /**
     * Handles all declaration specifiers on the left side, ie for const unsigned int x[3]; this
     * handles the "const unsigned int" part. This method assumes each element of declarationSpecifiers
     * contains at most 1 typeQualifier or typeSpecifier. Pointers, structs / unions and enums are properly
     * handled.
     *
     * @param declarationSpecifiers ArrayList of CParser.DeclarationSpecifier or equivalent type.
     *                              This type should have a typeSpecifier or a typeQualifier as a
     *                              direct child. Should have size > 0
     * @param scope Scope to evaluate in
     * @return Type, based on the left hand specifiers and qualifiers.
     */
    public AbstractTypeStorage handleDeclarationSpecifier(ArrayList<ParseTree> declarationSpecifiers, AbstractScope scope) {
        // Sanity check
        if (declarationSpecifiers.size() == 0)
            throw new IllegalStateException("declarationSpecifiers has size 0, this is not allowed!");

        ArrayList<String> typeSpecifiers = new ArrayList<>();
        ArrayList<String> typeQualifiers = new ArrayList<>();
        ArrayList<String> storageSpecifiers = new ArrayList<>();
        ArrayList<String> functionSpecifiers = new ArrayList<>();

        int pointerLevel = 0;
        AbstractTypeStorage specialType = null; // Enum or struct/union

        // Both variables are used to check for conflicting definitions
        boolean seenSpecial    = false; // Not a special type like enum or struct or union
        boolean notSeenSpecial = false; // Not enum or struct or union

        for (ParseTree grandchild : declarationSpecifiers) {
            // Assumed: each child is a DeclarationSpecifier, so we peek down 1 more
            if (grandchild instanceof CParser.DeclarationSpecifierContext)
                grandchild = grandchild.getChild(0);

            if (grandchild instanceof CParser.TypeQualifierContext)
                typeQualifiers.add(grandchild.getText());

            else if (grandchild instanceof CParser.FunctionSpecifierContext)
                functionSpecifiers.add(grandchild.getText());

            else if (grandchild instanceof CParser.StorageClassSpecifierContext)
                storageSpecifiers.add(grandchild.getText());

            // Type specifiers can be nested
            else if (grandchild instanceof CParser.TypeSpecifierContext || grandchild instanceof CParser.SpecifierQualifierListContext) {
                Queue<ParseTree> toSearch = new LinkedList<>();
                toSearch.add(grandchild);

                while (toSearch.size() > 0) {
                    ParseTree ctx = toSearch.remove();

                    // Just a string, like 'int'
                    if (ctx instanceof TerminalNodeImpl) {
                        notSeenSpecial = true;
                        typeSpecifiers.add(ctx.getText());
                    }

                    // Pointer star in type
                    else if (ctx instanceof CParser.PointerContext)
                        pointerLevel++;

                    // More levels to search
                    else if (ctx instanceof CParser.TypeSpecifierContext || ctx instanceof CParser.SpecifierQualifierListContext) {
                        for (int j = 0; j < ctx.getChildCount(); j++)
                            toSearch.add(ctx.getChild(j));
                    }

                    // Create an enum
                    else if (ctx instanceof CParser.EnumSpecifierContext) {
                        seenSpecial = true;
                        specialType = compiler.enumHandler.enumFromEnumSpecifierContext((CParser.EnumSpecifierContext) ctx, scope);

                        if (!(specialType instanceof InvalidTypeStorage))
                            scope.customEnumTypes.put(((EnumStorage)specialType).name, (EnumStorage)specialType);
                    }

                    // Create a struct
                    else if (ctx instanceof CParser.StructOrUnionSpecifierContext) {
                        seenSpecial = true;
                        specialType = compiler.structOrUnionHandler.structOrUnionFromStructOrUnionSpecifierContext((CParser.StructOrUnionSpecifierContext) ctx, scope);

                        if (specialType instanceof StructOrUnionStorage)
                            scope.addStructOrUnionType((StructOrUnionStorage)specialType);
                    }

                    else if (ctx instanceof CParser.TypedefNameContext) {
                        // Use of a custom typedef type
                        if (ctx.getParent().getParent() instanceof CParser.DeclarationSpecifierContext) {
                            String typeName = ctx.getText();

                            AbstractTypeStorage type = scope.getTypedef(typeName);
                            if (type == null)
                                type = compiler.machine.primitives.getBaseType(typeName);
                            if (type == null)
                                compiler.error("unknown type name '" + typeName + "'", ctx);

                            specialType = type;
                            seenSpecial = true;
                        }

                        // Otherwise ignore this for struct definitions, variable name is handled elsewhere
                    }

                    // No idea what it is
                    else {
                        compiler.parserError("found " + ctx.getClass().getSimpleName() + " as child of TypeSpecifier, don't know how to handle", grandchild);
                        return null;
                    }
                }
            }
        }

        // Syntax error check
        if ((seenSpecial && notSeenSpecial) || (!seenSpecial && !notSeenSpecial))
            compiler.error("two or more data types in declaration specifiers", declarationSpecifiers.get(0).getParent());

        // Reverse to proper orientation, it's backwards because of search order
        Collections.reverse(typeSpecifiers);

        AbstractTypeStorage returned = specialType == null ?
                compiler.machine.primitives.getBaseType(typeSpecifiers.get(typeSpecifiers.size() - 1)) :
                specialType;

        try { returned.addSpecifiers(typeSpecifiers, typeQualifiers, storageSpecifiers, functionSpecifiers); }
        catch(InvalidSpecifierException e) {
            compiler.error(e.getMessage(), declarationSpecifiers.get(0).getParent());
        }

        if (pointerLevel > 0)
            returned = new PointerTypeStorage(returned, pointerLevel);

        returned = compiler.cacheType(returned);
        return returned;
    }

    /**
     * <p>Solves a DirectDeclarator, which is usually the right-hand portion of a variable
     * declaration.</p>
     *
     * <p>For instance, in int (*var)[3];, the base type is "int", while the
     * direct declarator of (*var)[3] adds additional information: the type is a pointer to
     * an int array of size 3.</p>
     *
     * <p>In the example above, "type" is an int, and after recursively parsing, we get:
     * <ul>
     *     <li>Type = int[3]</li>
     *     <li>Type = pointer to int[3]</li>
     * </ul>
     * Note the type solving is down in a top-down manner, first we apply the [3], then the *</p>
     *
     * @param tree DirectDeclaratorContext or any parent or direct child to parse
     * @param type Initial type, this is the typeSpecifier for bottom most level.
     *             Recursive definitions will pass the type up.
     * @return variable name, AbstractTypeStorage: type of the variable
     */
    public NameAndType solveDeclarator(ParseTree tree, AbstractTypeStorage type, AbstractScope scope) {
        String name = "";
        int pointerLevel = 0;

        // Skip pointless intermediate nodes
        while (tree.getChildCount() == 1) {
            // Only child is terminal node, meaning there are no type modifiers
            // The child contains the name of the variable
            if (tree instanceof CParser.DirectDeclaratorContext && tree.getChildCount() == 1 && tree.getChild(0) instanceof TerminalNodeImpl) {
                name = tree.getChild(0).getText();
                return new NameAndType(name, type);
            }
            tree = tree.getChild(0);
        }

        // Increment pointer level
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i) instanceof CParser.PointerContext)
                pointerLevel++;
        }

        if (tree instanceof CParser.DirectDeclaratorContext) {
            // The not TerminalNodeImpl check is to ignore cases where an expression is just
            // wrapped in ( ), ie ((int)). Functions and arrays always have [type] ( ... ), or [type] [ ... ]
            if (!(tree.getChild(0) instanceof TerminalNodeImpl)) {
                // Function pointer
                // name ()
                // name ( paramList )
                if (
                        (tree.getChildCount() == 3 && tree.getChild(2).getText().equals(")")) ||
                        (tree.getChildCount() == 4 && tree.getChild(3).getText().equals(")"))) {

                    ArrayList<FunctionParameter> parameters = new ArrayList<>();
                    compiler.definitionHandler.handleFunctionParameterList(tree, 2, parameters, scope);
                    type = new FunctionTypeStorage(tree.getChild(0).getText(), type, parameters, true);
                }

                // Array
                if (
                        (tree.getChildCount() == 3 && tree.getChild(2).getText().equals("]")) ||
                        (tree.getChildCount() == 4 && tree.getChild(3).getText().equals("]"))) {

                    // Expected [??], [, AssignmentExpression, ]
                    if (tree.getChildCount() == 4 && !(tree.getChild(2) instanceof CParser.AssignmentExpressionContext))
                        compiler.parserError("don't understand array size given", tree.getChild(2));

                    if (tree.getChildCount() == 4) {
                        int length = ((ConstantValue)compiler.constantExprHandler.evalFileScopeExpr(tree.getChild(2))).getBigIntegerValue().intValue();
                        type = new ArrayTypeStorage(type, length);
                    }
                    else {
                        type = new IncompleteArrayTypeStorage(type);
                    }
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
            if (child instanceof CParser.DirectDeclaratorContext || child instanceof CParser.DeclaratorContext)
                return solveDeclarator(child, type, scope);
        }

        return new NameAndType(name, type);
    }

    /**
     * Handle a ParameterDeclarationContext, returning the name and type of the function parameter.
     * @param context A ParameterDeclarationContext
     * @return Function parameter, with a null name of the parameter is unnamed
     */
    public FunctionParameter handleParameterDeclaration(CParser.ParameterDeclarationContext context, AbstractScope scope) {
        if (!(context.getChild(0) instanceof CParser.DeclarationSpecifiers2Context) && !(context.getChild(0) instanceof CParser.DeclarationSpecifiersContext))
            compiler.parserError("expected declaration specifiers as child, got " + context.getChild(0).getClass().getSimpleName(), context);

        AbstractTypeStorage type = handleDeclarationSpecifier(context.getChild(0), scope);

        if (context.getChild(1) instanceof CParser.DeclaratorContext) {
            // Get first child that's a declarator and recursively solve the type
            NameAndType result = solveDeclarator(context.getChild(1), type, scope);
            return new FunctionParameter(result.type, result.name);
        }
        return new FunctionParameter(type, null);
    }

    /**
     * Fills an arraylist of function parameters passed. This will handle function parameter declarations
     * in a consistent manner across all files.
     *
     * @param tree Root node of the function declaration, should contain (, ParameterTypeList, ) as some of its direct children
     * @param parameterIndex Index of the ParameterTypeList child. Depending on where the function parameter list is declared
     *                       this can vary. It's assumed the parameterTypeList will always be the 2nd last child, with the last
     *                       child being a ')'.
     * @param parameters Array list of function parameters to fill
     * @param scope Scope to evaluate in
     */
    public void handleFunctionParameterList(ParseTree tree, int parameterIndex, ArrayList<FunctionParameter> parameters, AbstractScope scope) {
        if (tree.getChildCount() == parameterIndex + 2 && tree.getChild(parameterIndex) instanceof CParser.ParameterTypeListContext) {
            ParseTree parameterTypeList = tree.getChild(parameterIndex);
            ArrayList<ParseTree> parameterDeclarations = CompilerUtil.getTreesByRule(parameterTypeList, ctx -> ctx instanceof CParser.ParameterDeclarationContext, true);
            HashSet<String> parameterNames = new HashSet<>();
            boolean seenUnnamedVoid = false;

            for (int i = 0; i < parameterDeclarations.size(); i++) {
                ParseTree param = parameterDeclarations.get(i);
                FunctionParameter parameter = compiler.definitionHandler.handleParameterDeclaration((CParser.ParameterDeclarationContext) param, scope);

                if (parameter.type.isVoid()) {
                    if (!parameter.unnamed)
                        compiler.warning("parameter " + (i + 1) + " ('" + parameter.name + "') has void type", param);
                    else seenUnnamedVoid = true;
                }
                if (parameter.name != null && parameterNames.contains(parameter.name))
                    compiler.error("redefinition of parameter '" + parameter.name + "'", param);

                parameterNames.add(parameter.name);
                parameters.add(parameter);
            }

            if (parameterTypeList.getChildCount() > 0 && parameterTypeList.getChild(parameterTypeList.getChildCount() - 1).getText().equals("..."))
                parameters.add(new FunctionParameter(null, "..."));

            if (seenUnnamedVoid && parameters.size() > 1)
                compiler.error("'void' must be the only parameter", tree.getChild(parameterIndex + 1));
        }
    }
}
