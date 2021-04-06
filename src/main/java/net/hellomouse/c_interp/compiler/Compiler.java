package net.hellomouse.c_interp.compiler;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.scope.GlobalScope;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.compiler.handlers.*;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Compiler instance for a given (group) of files
 * @author Bowserinator
 */
public class Compiler {
    // All various handlers
    public final DefinitionHandler definitionHandler = new DefinitionHandler(this);
    public final FileScopeExprHandler constantExprHandler = new FileScopeExprHandler(this);
    public final StructOrUnionHandler structOrUnionHandler = new StructOrUnionHandler(this);
    public final EnumHandler enumHandler = new EnumHandler(this);
    public final FunctionDefinitionHandler functionDefinitionHandler = new FunctionDefinitionHandler(this);
    public final StatementHandler statementHandler = new StatementHandler(this);
    public final ExpressionHandler expressionHandler = new ExpressionHandler(this);
    public final TypeNameHandler typeNameHandler = new TypeNameHandler(this);

    public final GlobalScope globalScope = new GlobalScope();

    public final HashMap<String, AbstractTypeStorage> typeCache = new HashMap<>(); // ID : type
    public final HashMap<String, Integer> typeIdMap = new HashMap<>(); // ID : numeric ID

    public final Machine machine;

    public CompilerInstructions instructions = new CompilerInstructions();

    private String[] lines;
    private String funcName;
    private String fileName = "unknown_file";

    /**
     * Construct a compiler instance
     * @param machine Machine instance
     */
    public Compiler(Machine machine) {
        this.machine = machine;
    }

    /**
     * Compile the result from a CParser
     * @param parser CParser of the original program text
     * @param stream CharStream of the original program text
     */
    public void compile(CParser parser, CharStream stream) {
        lines = stream.getText(new Interval(0, stream.size())).split("\n");
        // instructions.clear();

        RuleContext root = parser.translationUnit();
        ArrayList<ParseTree> externalDeclarations = CompilerUtil.getTreesByRule(root, context -> context instanceof CParser.ExternalDeclarationContext, true);

        for (ParseTree tree : externalDeclarations) {
            if (tree instanceof CParser.ExternalDeclarationContext)
                parse(tree);
        }

        // instructions.printInstructions();
    }

    /**
     * Generate instructions from a tree. Will traverse down ExternalDeclarationContext.
     * @param context ExternalDeclarationContext
     */
    private void parse(ParseTree context) {
        if (context instanceof CParser.ExternalDeclarationContext)
            context = context.getChild(0);

        if (context instanceof CParser.FunctionDefinitionContext)
            functionDefinitionHandler.handleFunctionDefinition((CParser.FunctionDefinitionContext)context);
        else
            statementHandler.handleDefinitionOrTypedef(context, globalScope);
    }

    public AbstractTypeStorage cacheType(AbstractTypeStorage type) {
        if ((type.getStorageSpecifiers() & StorageSpecifier.TYPEDEF) != 0)
            return type;

        String id = type.getId();

        if (typeCache.containsKey(id))
            return typeCache.get(id);

        typeCache.put(id, type);
        typeIdMap.put(id, typeCache.size());
        return type;
    }

    /**
     * Print a warning message to the terminal
     * @param text Warning message, ie "invalid declarator"
     * @param node ParseTree that caused the warning, will be pointed to
     * @param flag Optional flag for the warning, ie "-Wcomment", used for warning flags.
     *             Should be the most specific possible flag. If the warning is universal,
     *             set flag to null.
     */
    public void warning(String text, ParseTree node, @Nullable String flag) {
        printMsg(flag != null ? text + " [" + flag + "]" : text, node, "warning");
    }
    public void warning(String text, ParseTree node) {
        warning(text, node, null);
    }

    /**
     * Prints a note message to the terminal
     * @param text Text content to getStringValue
     * @param node ParseTree that is being noted, will be pointed to
     */
    public void note(String text, ParseTree node) {
        printMsg(text, node, "note");
    }

    /**
     * Prints an error message to the terminal and can throw an error
     * @param text Error message
     * @param invalidNode ParseTree that caused the error, will be pointed to
     * @param fatal Should it halt handling of the current tree by throwing an exception?
     */
    public void error(String text, ParseTree invalidNode, boolean fatal) {
        printMsg(text, invalidNode, "error");
        if (fatal) throw new IllegalStateException("");
    }
    public void error(String text, ParseTree invalidNode) {
        error(text, invalidNode, true);
    }

    /**
     * Prints an error message to the terminal and throws an error. Unlike error(), this
     * is used for parser failures rather than invalid C code
     * @param text Error message
     * @param invalidNode ParseTree that caused the error, will be pointed to
     */
    public void parserError(String text, ParseTree invalidNode) {
        error(text, invalidNode, true);
    }

    /**
     * Same as an fatal error, but no message will be printed to the terminal. Useful for
     * putting a note after an error message.
     */
    public void silentError() {
        throw new IllegalStateException("");
    }

    /**
     * Helper for printing a log message. Prints the current file + function, line and char
     * numbers, and the original line with a pointer to the node with the message.
     *
     * @param text Message to display
     * @param node ParseTree to point to
     * @param type Type of message (warning, error, note, etc...), appended before the text
     */
    private void printMsg(String text, ParseTree node, String type) {
        Pair<Integer, Integer> lineCharNumber = CompilerUtil.getLineCharNumber(node);

        // TODO: bind terminal
        if (funcName != null)
            System.out.println(fileName + ": In function '" + funcName + "':");

        System.out.print(fileName + ":" + lineCharNumber.a + ":" + lineCharNumber.b + ": ");
        System.out.println(type + ": " + text);
        System.out.println(" " + lines[lineCharNumber.a - 1]);
        System.out.println(" ".repeat(lineCharNumber.b + 1) + "^");
    }

    /**
     * Wrapper for adding an instruction to state
     * @param instr Instruction to add
     */
    public void addInstruction(AbstractInstruction instr) {
        instructions.addInstruction(instr);
    }

    /**
     * Enter a function, called by functionDefinitionHandler. Used for log messages
     * @param name Name of the function, ie 'main'
     */
    public void enterFunction(String name) { funcName = name; }

    /** Exit a function back into global scope. Used for log messages */
    public void exitFunction() { funcName = null; }

    /**
     * Enter a file name. Used for log messages
     * @param name Name of the file, ie 'my_program.c'
     */
    public void enterFile(String name) { fileName = name; }

    /** Exit a file. Reserved for future use */
    public void exitFile() {}
}
