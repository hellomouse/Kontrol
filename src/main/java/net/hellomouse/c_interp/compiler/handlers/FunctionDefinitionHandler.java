package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.storage.*;
import net.hellomouse.c_interp.common.scope.LocalScope;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.common.specifiers.TypeQualifier;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.common.storage_types.interfaces.IArray;
import net.hellomouse.c_interp.common.storage_types.interfaces.IIncompleteType;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import net.hellomouse.c_interp.instructions.IJumpInstruction;
import net.hellomouse.c_interp.instructions.expression.DefinitionInstruction;
import net.hellomouse.c_interp.instructions.expression.FunctionInstructions;
import net.hellomouse.c_interp.instructions.statement.ScopeInstructions;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;

/**
 * Handles a function definition. This does not handle prototypes, only function
 * definitions with a function body
 * @author Bowserinator
 */
public class FunctionDefinitionHandler extends AbstractHandler {
    public FunctionDefinitionHandler(Compiler compiler) { super(compiler); }

    /**
     *  <p>Handles a function definition. This does not handle prototypes, only function
     *  definitions with a function body. Automatically adds all relevant commands
     *  to the compiler directly.</p>
     *
     *  <p>Currently only evaluates in compiler.globalScope as all C functions can only
     *  be defined in the global scope</p>
     *
     * @param context Root node of the function definition
     */
    public void handleFunctionDefinition(CParser.FunctionDefinitionContext context) {
        // Return type handling
        // FunctionDefinition -> declarationSpecifiers, declarator,      compoundStatement
        //                       ^ return type          ^ name + params  ^ function body

        boolean noReturnType = context.getChild(0) instanceof CParser.DeclaratorContext;

        // Default return type is int
        AbstractTypeStorage returnType = noReturnType ?
                compiler.machine.primitives.getBaseType("int") :
                compiler.definitionHandler.handleDeclarationSpecifier(context.getChild(0), compiler.globalScope);

        if (returnType instanceof IIncompleteType) {
            compiler.error("return type is an incomplete type", context.getChild(0));
            return;
        }

        // Function name handling. Locations is different depending if function has parameters or not
        // - If non-zero # of parameters, it's in child(0) as a TypedefName
        // - If zero parameters, it's in child(1) under a directDeclarator
        String name;
        ArrayList<ParseTree> typedefName = CompilerUtil.getTreesByRule(context.getChild(0), ctx -> ctx instanceof CParser.TypedefNameContext, true);
        LocalScope scope = new LocalScope(compiler.globalScope, "func");

        if (typedefName.size() > 0) {
            name = typedefName.get(0).getText();
        } else {
            NameAndType result = compiler.definitionHandler.solveDeclarator(context.getChild(noReturnType ? 0 : 1), returnType, scope);
            name = result.name;

            // Cannot return arrays. Arrays of function definitions are valid in ANTLR4's grammar, but
            // do not meaningfully compile
            if (result.type instanceof IArray) {
                compiler.error(" expected '=', ',', ';', 'asm' or '__attribute__' before '{' token", context);
                return;
            }
        }

        if (noReturnType)
            compiler.warning("type defaults to 'int' in declaration of '" + name + "'", context, "-Wimplicit-int");

        ArrayList<FunctionParameter> parameters = new ArrayList<>();

        // Function parameter handling
        // declarator -> directDeclarator -> directDeclarator, (, parameterList, )

        if (context.getChild(1).getChild(0).getChildCount() == 4) {
            ParseTree parameterList = context.getChild(1).getChild(0);
            compiler.definitionHandler.handleFunctionParameterList(parameterList, 2, parameters, compiler.globalScope);
        }

        FunctionTypeStorage funcType = new FunctionTypeStorage(name, returnType, parameters, false);

        // Redefinition check
        if (compiler.globalScope.functionDeclarations.containsKey(name)) {
            FunctionTypeStorage existingFunction = compiler.globalScope.functionDeclarations.get(name);
            if (!funcType.equals(existingFunction))
                compiler.error("conflicting types for '" + name + "'", context);
            else if (!existingFunction.isPrototype)
                compiler.error("redefinition of '" + name + "'", context);
        }

        compiler.globalScope.functionDeclarations.put(name, funcType);
        compiler.addInstruction(new FunctionInstructions.FunctionDefinitionInstruction(returnType, name, parameters, funcType));

        int functionStartAddress = compiler.instructions.currentInstructionAddress();

        compiler.addInstruction(new ScopeInstructions.ScopeStartInstruction());
        compiler.addInstruction(new FunctionInstructions.LoadFunctionArguments(name));
        compiler.enterFunction(name);

        // Function body, should be a CompoundStatement
        ParseTree body = context.getChild(noReturnType ? 1 : 2);

        // Function body is not empty
        if (!(body.getChildCount() == 2 && body.getChild(1) instanceof TerminalNodeImpl)) {
            // Add __func__
            // static const char __func__[] = "function name";

            AbstractTypeStorage type = compiler.machine.primitives.getBaseType("char");
            type.setSpecifiers(0x0, TypeQualifier.CONST, StorageSpecifier.STATIC, 0x0);
            type = type.toPointer(1);

            Variable __func__ = new Variable("__func__", type, new ConstantValue(compiler.machine, '"' + name + '"'));
            scope.variables.put("__func__", __func__);
            compiler.addInstruction(new DefinitionInstruction(__func__, scope.getId()));

            // Put parameters in scope
            for (FunctionParameter param : parameters) {
                if (!param.vararg && !param.unnamed)
                    scope.variables.put(param.name, new PassedParameter(param.name, param.type));
            }

            // Check 1st child is blockItemList
            if (!(body.getChild(0) instanceof TerminalNodeImpl) || !(body.getChild(1) instanceof CParser.BlockItemListContext))
                compiler.parserError("invalid function body", body);

            ArrayList<ParseTree> code = CompilerUtil.getTreesByRule(body.getChild(1), ctx -> ctx instanceof CParser.BlockItemContext, true);

            for (ParseTree c : code)
                compiler.statementHandler.handleStatement(c, scope);
        }

        compiler.addInstruction(new ScopeInstructions.ScopeEndInstruction());
        compiler.addInstruction(new FunctionInstructions.FunctionEndInstruction());
        ((IJumpInstruction)compiler.instructions.getInstructions().get(functionStartAddress)).setAddress(compiler.instructions.currentInstructionAddress());

        scope.postProcess();

        compiler.exitFunction();
    }
}
