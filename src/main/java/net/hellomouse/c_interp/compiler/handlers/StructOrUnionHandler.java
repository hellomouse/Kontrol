package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.storage.NameAndType;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.scope.GlobalScope;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.IncompleteStructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.InvalidTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Handles declarations of structs / unions
 * @author Bowserinator
 */
public class StructOrUnionHandler extends AbstractHandler {
    public StructOrUnionHandler(Compiler compiler) { super(compiler); }

    /**
     * Construct a StructOrUnion type, or an incomplete variant of it from a context.
     * @param context Root node of the struct or union specifier
     * @param scope Scope to evaluate in
     * @return StructOrUnionStorage / IncompleteStructOrUnionStorage type
     */
    public AbstractTypeStorage structOrUnionFromStructOrUnionSpecifierContext(CParser.StructOrUnionSpecifierContext context, AbstractScope scope) {
        String name = "";

        ParseTree structDeclaratorList;
        boolean isStruct = context.getChild(0).getText().equals("struct");

        // structOrUnion name { struct declarator list }
        if (context.getChildCount() == 5 && context.getChild(3) instanceof CParser.StructDeclarationListContext) {
            name = context.getChild(1).getText();
            structDeclaratorList = context.getChild(3);
        }
        // structOrUnion { struct declarator list }
        else if (context.getChildCount() == 4 && context.getChild(2) instanceof CParser.StructDeclarationListContext) {
            structDeclaratorList = context.getChild(2);
        }
        // structOrUnion name
        else if (context.getChildCount() == 2 && context.getChild(1) instanceof TerminalNodeImpl) {
            // Incomplete declaration
            StructOrUnionStorage existingStructOrUnion = scope.getStructOrUnion(context.getChild(1).getText());
            if (existingStructOrUnion == null) {
                // Can't have incomplete structs in non-global scope
                if (!(scope instanceof GlobalScope))
                    return new InvalidTypeStorage();
                return new IncompleteStructOrUnionStorage(isStruct, context.getChild(1).getText());
            }
            return existingStructOrUnion;
        }
        // ???
        else {
            compiler.parserError("unknown struct declaration syntax", context);
            return null; // parserError should halt execution, this return is if IDE doesn't recognize
        }

        StructOrUnionStorage scopeType = scope.getStructOrUnion(name);
        if (scopeType != null) {
            compiler.error(
                scopeType.isStruct == isStruct ?
                    "redefinition of '" + scopeType.getFullName() + "'" :
                    "'" + name + "' defined as wrong kind of tag",
                context);
        }

        return new StructOrUnionStorage(isStruct, name, getStructMembers((CParser.StructDeclarationListContext)structDeclaratorList, scope));
    }

    /**
     * Handles all field declarations in the struct, and returns an array of members
     *
     * @param context StructDeclaratorList
     * @param scope Scope to evaluate in.
     * @return Array of added members
     */
    private ArrayList<NameAndType> getStructMembers(CParser.StructDeclarationListContext context, AbstractScope scope) {
        ArrayList<ParseTree> structDeclarations = CompilerUtil.getTreesByRule(context, ctx -> ctx instanceof CParser.StructDeclarationContext, true);
        ArrayList<NameAndType> declarations = new ArrayList<>();
        HashSet<String> memberNames = new HashSet<>();

        for (ParseTree declaration : structDeclarations)
            declarations.addAll(handleStructMember((CParser.StructDeclarationContext)declaration, scope, memberNames));
        return declarations;
    }

    /**
     * Handle a single declaration within the struct, ie int x, y; would be considered
     * a single declaration that creates two members, x and y. Returns an array of all members
     * declared.
     *
     * @param context StructDeclarationContext
     * @param scope Scope to evaluate in
     * @return Array of added members
     */
    private ArrayList<NameAndType> handleStructMember(CParser.StructDeclarationContext context, AbstractScope scope, HashSet<String> memberNames) {
        ArrayList<NameAndType> declarations = new ArrayList<>();

        if (!(context.getChild(0) instanceof CParser.SpecifierQualifierListContext))
            compiler.parserError("Expected specifierQualifierList for struct / union declaration, got " + context.getChild(0).getClass().getSimpleName(), context);

        ArrayList<ParseTree> declarationTrees = CompilerUtil.getTreesByRule(context.getChild(0), ctx -> ctx instanceof CParser.TypeSpecifierContext || ctx instanceof CParser.TypeQualifierContext, true);
        AbstractTypeStorage type = compiler.definitionHandler.handleDeclarationSpecifier(declarationTrees, scope);

        // Multiple declarations or complex declarations such as
        // int a, b; or int *a[3];
        ArrayList<ParseTree> definitionTrees = CompilerUtil.getTreesByRule(context.getChild(1), ctx -> ctx instanceof CParser.StructDeclaratorContext, true);
        for (ParseTree definition : definitionTrees) {
            NameAndType result = compiler.definitionHandler.solveDeclarator(definition, type, scope);
            checkMemberDuplicates(definition, memberNames, result.name);
            declarations.add(result);
        }

        // Just [varType] [varName], this uses TypedefName to store the variable name
        if (declarations.size() == 0) {
            ArrayList<ParseTree> varNames = CompilerUtil.getTreesByRule(context.getChild(0), ctx -> ctx instanceof CParser.TypedefNameContext, true);
            String name = varNames.get(0).getText();

            checkMemberDuplicates(varNames.get(0), memberNames, name);
            declarations.add(new NameAndType(name, type));
        }

        return declarations;
    }

    /**
     * Check for duplicate member fields and errors, otherwise adds name to the memberNames set
     * @param context ParseTree pointing to duplicate member declaration
     * @param memberNames Set of all member names so far
     * @param name Member name to check
     */
    private void checkMemberDuplicates(ParseTree context, HashSet<String> memberNames, String name) {
        if (memberNames.contains(name))
            compiler.error("duplicate member '" + name + "'", context);
        memberNames.add(name);
    }
}
