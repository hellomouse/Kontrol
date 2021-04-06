package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.expressions.FunctionCallExpression;
import net.hellomouse.c_interp.common.expressions.FunctionReturnExpression;
import net.hellomouse.c_interp.common.expressions.RegisterExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.labels.NamedLabel;
import net.hellomouse.c_interp.common.expressions.labels.SwitchCaseLabel;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractOperation;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.scope.AbstractScope;
import net.hellomouse.c_interp.common.scope.LocalScope;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.compiler.CompilerUtil;
import net.hellomouse.c_interp.instructions.expression.DefinitionInstruction;
import net.hellomouse.c_interp.instructions.expression.ExpressionInstruction;
import net.hellomouse.c_interp.instructions.statement.JumpInstructions;
import net.hellomouse.c_interp.instructions.statement.RegisterInstruction;
import net.hellomouse.c_interp.instructions.statement.SelectionInstructions;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;


/**
 * Handles an statement, which are non-definitions in function bodies. Examples include
 * expressions like a++, while(true) {}, if(...){}, x=1. This handler directly adds
 * instructions to the compiler when run.
 *
 * @author Bowserinator
 */
public class StatementHandler extends AbstractHandler {
    public StatementHandler(Compiler compiler) { super(compiler); }

    /**
     * <p>Generic statement handler. If you don't know the type of the statement, use this one.
     * Will traverse down to the first meaningful child, then automatically determine how to
     * handle the statement. Will add the correct instructions for the statement to the compiler.</p>
     *
     * <p>Note: this should only be run in the context of a function body.</p>
     *
     * @param context Root node of the statement. All children must be statements, declarations or expressions
     * @param scope Scope to evaluate statement in, used for compile time checks
     */
    public void handleStatement(ParseTree context, LocalScope scope) {
        // Traverse down to first meaningful context
        while (context.getChildCount() == 1) {
            context = context.getChild(0);

            // Expression statement -> expression, ;, skip the semicolon
            while (context.getChildCount() == 2 && context instanceof CParser.ExpressionStatementContext)
                context = context.getChild(0);
        }

        // Recursively solve sub-statements
        if (context instanceof CParser.CompoundStatementContext) {
            LocalScope newScope = new LocalScope(scope, "block");
            ArrayList<ParseTree> statements = CompilerUtil.getTreesByRule(context, ctx -> ctx instanceof CParser.BlockItemContext, true);

            for (ParseTree statement : statements)
                handleStatement(statement, newScope);
        }

        // Loops
        else if (context instanceof CParser.IterationStatementContext)
            handleIterationStatement((CParser.IterationStatementContext)context, scope);
        // Declaration of a local variable
        else if (context instanceof CParser.DeclarationContext || context instanceof CParser.ForDeclarationContext)
            handleDefinitionOrTypedef(context, scope);
        // if / else / switch
        else if (context instanceof CParser.SelectionStatementContext)
            handleSelectionStatement(context, scope);
        // return / continue / break / goto
        else if (context instanceof CParser.JumpStatementContext)
            handleJumpStatement(context, scope);
        // label:; / case x: / default:
        else if (context instanceof CParser.LabeledStatementContext)
            handleLabeledStatement(context, scope);
        // Expression (we shouldn't reach a terminal node before reaching an expression)
        // If we do, the terminal node is either semicolon or a non-meaningful expression, ie
        // "1;" or "a;"
        else if (!(context instanceof TerminalNodeImpl))
            handleExpressionStatement(context, scope);
    }

    /**
     * Handle a variable definition or typedef, will create the variable and potentially modify the scope
     * @param context Root node of the definition, ie a DeclarationContext or ForDeclarationContext
     * @param scope Scope to evaluate, will be modified
     */
    public void handleDefinitionOrTypedef(ParseTree context, AbstractScope scope) {
        // This adds variables to scope
        ArrayList<Variable> definitions = compiler.definitionHandler.handleDeclaration(context, scope);

        for (Variable definition : definitions) {
            // Not a typedef, add definition instruction
            if ((definition.getType().getStorageSpecifiers() & StorageSpecifier.TYPEDEF) == 0) {
                if (definition.getValue() != null)
                    definition.setValue(getExpressionAfterDecomposition(definition.getValue(), scope));
                compiler.addInstruction(new DefinitionInstruction(definition, scope.getId()));
            }
        }
    }

    /**
     * Add instructions for an iteration statement (for / while / do-while)
     * @param context Root node of the statement
     * @param scope Scope to evaluate in
     */
    private void handleIterationStatement(CParser.IterationStatementContext context, LocalScope scope) {
        LocalScope loopScope = new LocalScope(scope, "iter");

        int loopStartAddress = -1;    // Where to jump back when looping
        JumpInstructions.JumpIfNotInstruction whileLoop = null;

        switch (context.getChild(0).getText()) {
            // for ( forCondition ) statement
            case "for" -> {
                if (context.getChildCount() != 5 || !(context.getChild(2) instanceof CParser.ForConditionContext) || !(context.getChild(4) instanceof CParser.StatementContext))
                    compiler.parserError("malformed for loop", context);

                // init-clause, cond-expression, and iteration-expression are all optional. If cond-expression is omitted,
                // it is replaced with a non-zero integer constant, which makes the loop endless:

                ParseTree declarationOrAssignmentNode = null;
                ParseTree conditionNode = null;
                ParseTree iterationNode = null;

                ParseTree forStatement = context.getChild(2);
                int expressionIndex = 0;

                for (int i = 0; i < forStatement.getChildCount(); i++) {
                    if (forStatement.getChild(i).getText().equals(";"))
                        expressionIndex++;
                    else if (forStatement.getChild(i) instanceof CParser.ForDeclarationContext)
                        declarationOrAssignmentNode = forStatement.getChild(i);
                    else if (forStatement.getChild(i) instanceof CParser.ExpressionContext && expressionIndex == 0)
                        declarationOrAssignmentNode = forStatement.getChild(i);
                    else if (forStatement.getChild(i) instanceof CParser.ForExpressionContext) {
                        // For condition
                        if (expressionIndex == 1)
                            conditionNode = forStatement.getChild(i);
                            // Iteration expression
                        else if (expressionIndex == 2)
                            iterationNode = forStatement.getChild(i);

                            // Should never happen
                        else {
                            compiler.parserError("cannot have a forStatement here", forStatement);
                        }
                    }
                }

                // DECLARATION / ASSIGNMENT
                if (declarationOrAssignmentNode != null)
                    handleStatement(declarationOrAssignmentNode, loopScope);

                compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("loop start"));
                loopStartAddress = compiler.instructions.currentInstructionAddress();

                // loopScope acts as wrapper for declaration only
                // subLoopScopes contains inner loop variables (sorry for naming)
                LocalScope subLoopScope = new LocalScope(loopScope, "loop");
                subLoopScope.markAsLoop();

                // WHILE / ... / INCREMENT_EXPR / END_LOOP
                IExpression expr = conditionNode != null ?
                        compiler.expressionHandler.handleExpression(conditionNode, subLoopScope) : // Parse expr
                        new ConstantValue(compiler.machine, "1"); // Non-zero constant so loop continues forever
                expr = getExpressionAfterDecomposition(expr, subLoopScope);

                whileLoop = new JumpInstructions.JumpIfNotInstruction("for (while) loop", expr, -1);
                compiler.addInstruction(whileLoop);

                handleStatement(context.getChild(4), subLoopScope);
                subLoopScope.setContinueAddress(compiler.instructions.currentInstructionAddress());

                if (iterationNode != null)
                    handleStatement(iterationNode, subLoopScope);

                // Break jumps to here
                subLoopScope.setBreakAddress(compiler.instructions.currentInstructionAddress());
            }
            // while ( expression ) statement
            case "while" -> {
                if (context.getChildCount() != 5 || !(context.getChild(2) instanceof CParser.ExpressionContext) || !(context.getChild(4) instanceof CParser.StatementContext))
                    compiler.parserError("malformed while loop", context);

                compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("loop start"));
                loopStartAddress = compiler.instructions.currentInstructionAddress();
                loopScope.markAsLoop();

                // WHILE / ... / END_LOOP
                IExpression expr = compiler.expressionHandler.handleExpression(context.getChild(2), scope);
                expr = getExpressionAfterDecomposition(expr, loopScope);

                whileLoop = new JumpInstructions.JumpIfNotInstruction("while loop", expr, -1);
                compiler.addInstruction(whileLoop);
                handleStatement(context.getChild(4), loopScope);
                loopScope.setContinueAddress(compiler.instructions.currentInstructionAddress());
            }
            // do statement while ( expression )
            case "do" -> {
                if (context.getChildCount() < 6 || !(context.getChild(4) instanceof CParser.ExpressionContext) || !(context.getChild(1) instanceof CParser.StatementContext))
                    compiler.parserError("malformed do while loop", context);

                compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("loop start"));
                loopStartAddress = compiler.instructions.currentInstructionAddress();
                loopScope.markAsLoop();

                // DO-WHILE / ... / END_LOOP
                handleStatement(context.getChild(1), loopScope);

                IExpression expr = compiler.expressionHandler.handleExpression(context.getChild(4), scope);
                expr = getExpressionAfterDecomposition(expr, loopScope);

                whileLoop = new JumpInstructions.JumpIfNotInstruction("do while loop", expr, -1);
                compiler.addInstruction(whileLoop);
                loopScope.setContinueAddress(compiler.instructions.currentInstructionAddress());
            }
        }

        if (whileLoop == null) {
            compiler.parserError("Malformed loop expression", context);
            return;
        }

        compiler.addInstruction(new JumpInstructions.JumpInstruction("end loop", loopStartAddress));
        whileLoop.setAddress(compiler.instructions.currentInstructionAddress());
        loopScope.setBreakAddress(compiler.instructions.currentInstructionAddress()); // Break for non-for loops
    }

    /**
     * Add expression instructions using result from compiler.expressionHandler
     * @param context Expression node
     * @param localScope Scope to evaluate in
     */
    private void handleExpressionStatement(ParseTree context, LocalScope localScope) {
        IExpression expr = compiler.expressionHandler.handleExpression(context, localScope);
        decomposeExpression(expr, 0, true, false, localScope);
    }

    /**
     * Handle selection statements such as if / else / switch
     * @param context Node for the first selection statement
     * @param scope Scope to evaluate the statement in
     */
    private void handleSelectionStatement(ParseTree context, LocalScope scope) {
        LocalScope localScope = new LocalScope(scope, "selection");
        ArrayList<JumpInstructions.JumpInstruction> branches = new ArrayList<>();

        if (context.getChild(0).getText().equals("if")) {
            // if ( expression ) statement
            if (context.getChildCount() == 5 && context.getChild(2) instanceof CParser.ExpressionContext) {
                // IF / ... / END_SELECT
                IExpression expr = compiler.expressionHandler.handleExpression(context.getChild(2), scope);
                expr = getExpressionAfterDecomposition(expr, localScope);

                JumpInstructions.JumpIfInstruction ifInstr = new JumpInstructions.JumpIfNotInstruction("if statement", expr, -1);

                branches.add(ifInstr);
                compiler.addInstruction(ifInstr);
                handleStatement(context.getChild(4), localScope);
            }

            // if ( expression ) statement else statement
            // Note: there is no else if, in the AST it becomes if() else { if () {} ... }
            // so each if statement is effectively considered to have two branches
            else if (context.getChildCount() == 7 && context.getChild(2) instanceof CParser.ExpressionContext) {
                // IF / ... / ELSE / ... / END_SELECT

                IExpression expr = compiler.expressionHandler.handleExpression(context.getChild(2), scope);
                expr = getExpressionAfterDecomposition(expr, localScope);

                JumpInstructions.JumpInstruction ifInstr = new JumpInstructions.JumpIfNotInstruction("if statement", expr, -1);

                // if ...
                compiler.addInstruction(ifInstr);
                handleStatement(context.getChild(4), localScope);

                // Skip the else
                JumpInstructions.JumpInstruction jumpToEnd = new JumpInstructions.JumpInstruction("if end", -1);
                compiler.addInstruction(jumpToEnd);
                branches.add(jumpToEnd);

                // else ...
                compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("else statement"));
                ifInstr.setAddress(compiler.instructions.currentInstructionAddress());
                handleStatement(context.getChild(6), localScope);
            }

            compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("end if"));
            for (JumpInstructions.JumpInstruction ifInstr : branches)
                ifInstr.setAddress(compiler.instructions.currentInstructionAddress());
        }

        else if (context.getChild(0).getText().equals("switch")) {
            // switch ( expression ) statement
            //   - labeled statement
            //   - case constantExpression : statement

            if (context.getChildCount() != 5 || !(context.getChild(1) instanceof TerminalNodeImpl) || !(context.getChild(3) instanceof TerminalNodeImpl))
                compiler.parserError("malformed switch statement", context);

            IExpression expression = compiler.expressionHandler.handleExpression(context.getChild(2), scope);
            expression = getExpressionAfterDecomposition(expression, localScope);
            ArrayList<ParseTree> blockItems = CompilerUtil.getTreesByRule(context.getChild(4), ctx -> ctx instanceof CParser.BlockItemContext, true);

            // SWITCH / ... / END_SELECT
            SelectionInstructions.SwitchInstruction switchCaseInstruction = new SelectionInstructions.SwitchInstruction(expression);
            compiler.addInstruction(switchCaseInstruction);
            localScope.markAsSwitch();

            for (ParseTree item : blockItems)
                handleStatement(item, localScope);

            compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("switch end"));

            switchCaseInstruction.setCases(localScope.switchLabels);
            switchCaseInstruction.setBreakAddress(compiler.instructions.currentInstructionAddress());
            localScope.setBreakAddress(compiler.instructions.currentInstructionAddress());
        }
    }

    /**
     * Handle label statements, such as label:, case x: and default:
     * @param labeledStatement Context for the statement
     * @param localScope Scope to evaluate in
     */
    private void handleLabeledStatement(ParseTree labeledStatement, LocalScope localScope) {
        ParseTree statements;
        String label = labeledStatement.getChild(0).getText();

        // default : statements
        if (label.equals("default")) {
            // DEFAULT / ...
            compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("default:"));

            LocalScope switchScope = ((LocalScope)localScope.getSwitchScope());
            SwitchCaseLabel switchCaseLabel = new SwitchCaseLabel(null, compiler.instructions.currentInstructionAddress());

            if (switchScope.switchLabelNames.contains("default")) {
                compiler.error("multiple default labels in one switch", labeledStatement);
                return;
            }

            switchScope.addSwitchLabel(switchCaseLabel);
            statements = labeledStatement.getChild(2);
        }
        // case expression : statements
        else if (label.equals("case")){
            // CASE / ...
            IExpression caseExpression = compiler.expressionHandler.handleExpression(labeledStatement.getChild(1), localScope);

            // Case must have an integer type. Enums are internally represented as ConstantValues as well as int literals
            if (!(caseExpression instanceof ConstantValue) || !((ConstantValue)caseExpression).isInt())
                compiler.error("case label does not reduce to an integer constant", labeledStatement.getChild(1));

            compiler.addInstruction(new JumpInstructions.LabeledNoOpInstruction("case " + caseExpression + ":"));

            LocalScope switchScope = ((LocalScope)localScope.getSwitchScope());
            SwitchCaseLabel switchCaseLabel = new SwitchCaseLabel((ConstantValue)caseExpression, compiler.instructions.currentInstructionAddress());

            if (switchScope.switchLabelNames.contains(switchCaseLabel.value.toString())) {
                compiler.error("duplicate case value", labeledStatement);
                return;
            }

            switchScope.addSwitchLabel(switchCaseLabel);
            statements = labeledStatement.getChild(3);
        }
        // label : statement
        else {
            // LABEL / ...
            compiler.addInstruction(new JumpInstructions.LabelInstruction(label));
            localScope.getFunctionScope().labels.put(label, new NamedLabel(label, compiler.instructions.currentInstructionAddress()));
            statements = labeledStatement.getChild(2);
        }
        handleStatement(statements, localScope);
    }

    /**
     * Handle jump statements, such as goto, continue, break or return
     * Adds instruction to compiler instruction list
     *
     * @param context Context of the Jump statement
     * @param scope Scope to evaluate in
     */
    private void handleJumpStatement(ParseTree context, LocalScope scope) {
        String type = context.getChild(0).getText();

        switch (type) {
            // goto label ;
            case "goto":
                if (context.getChildCount() != 3)
                    compiler.parserError("malformed goto statement", context);

                JumpInstructions.GotoInstruction gotoInstruction = new JumpInstructions.GotoInstruction(context.getChild(1).getText());
                compiler.addInstruction(gotoInstruction);
                scope.getFunctionScope().gotoInstructions.add(gotoInstruction);
                break;
            // continue
            case "continue":
                if (!scope.isInLoop()) {
                    compiler.error("continue statement not within a loop", context);
                    return;
                }

                JumpInstructions.ContinueInstruction continueInstruction = new JumpInstructions.ContinueInstruction();
                ((LocalScope)scope.getLoopScope()).continueInstructions.add(continueInstruction);
                compiler.addInstruction(continueInstruction);
                break;
            // break
            case "break":
                if (!scope.isInLoop() && !scope.isInSwitch()) {
                    compiler.error("break statement not within loop or switch", context);
                    return;
                }

                JumpInstructions.BreakInstruction breakInstruction = new JumpInstructions.BreakInstruction();
                ((LocalScope)scope.getLoopOrSwitchScope()).breakInstructions.add(breakInstruction);
                compiler.addInstruction(breakInstruction);
                break;
            // returns
            case "return":
                // return ;
                if (context.getChildCount() == 2)
                    compiler.addInstruction(new JumpInstructions.ReturnInstruction(null));

                // return expression ;
                else if (context.getChildCount() == 3 && context.getChild(1) instanceof CParser.ExpressionContext) {
                    IExpression expr = compiler.expressionHandler.handleExpression(context.getChild(1), scope);
                    expr = getExpressionAfterDecomposition(expr, scope);
                    compiler.addInstruction(new JumpInstructions.ReturnInstruction(expr));
                }

                else { compiler.parserError("malformed return statement", context); }
                break;
            default:
                compiler.parserError("jump statement " + type + " is not implemented", context);
        }
    }

    /**
     * Add instructions for an expression, decomposing it recursively. For example, a large expression such
     * as x += 2 * y + 3 * z would be broken up into several expressions using temporary "registers", then each
     * instruction added individually.
     *
     * @param expression Expression to decompose
     * @param register Register id to assign to, used for recursion. Not used at top level
     * @param topLevel Is it a top level call, used for recursion. Set to true unless you know what you're doing
     * @param skipTopLevel Skip top level expression instruction, used if the result is used elsewhere (ie, in a declaration,
     *                     where a separate instruction adds the expression)
     * @param scope Scope this is evaluated in
     */
    private void decomposeExpression(IExpression expression, int register, boolean topLevel, boolean skipTopLevel, AbstractScope scope) {
        if (expression instanceof AbstractOperation) {
            AbstractOperation operation = (AbstractOperation)expression;
            ArrayList<IExpression> operands = operation.getOperands();

            for (int i = 0; i < operands.size(); i++) {
                // If operation contains more sub operations, optimize them
                // Each sub operation has $i = [result of operation], where
                // $i is the ith register. The operation's operands will be changed
                // to use these new registers
                if (operands.get(i) instanceof AbstractOperation && !operation.containsNoSubOperations()) {
                    decomposeExpression(operands.get(i), i, false, skipTopLevel, scope);
                    operation.setOperand(i, new RegisterExpression(i, scope.getId()));
                }
                // Reduce function operations
                else if (operands.get(i) instanceof FunctionCallExpression) {
                    decomposeExpression(operands.get(i), i, false, skipTopLevel, scope);
                    operation.setOperand(i, new RegisterExpression(i, scope.getId()));
                }
            }
        }

        // Function expressions become two instructions:
        // - Function call (pushes stack frame)
        // - Function return (pops stack frame)
        if (expression instanceof FunctionCallExpression) {
            FunctionCallExpression fExpression = (FunctionCallExpression)expression;
            for (int i = 0; i < fExpression.arguments.size(); i++)
                fExpression.arguments.set(i, getExpressionAfterDecomposition(fExpression.arguments.get(i), scope));

            compiler.addInstruction(new RegisterInstruction(register, expression, scope.getId()));
            compiler.addInstruction(new RegisterInstruction(register, new FunctionReturnExpression(expression.getType()), scope.getId()));
        }

        else if (topLevel && !skipTopLevel)
            compiler.addInstruction(new ExpressionInstruction(expression));
        else if (!topLevel)
            compiler.addInstruction(new RegisterInstruction(register, expression, scope.getId()));
    }

    /**
     * Returns the new expression after decomposition. Will decompose the expression
     * and return the new expression to replace the original expression.
     * @param expression Expression to decompose
     * @param scope Scope to evaluate in
     * @return New expression
     */
    public IExpression getExpressionAfterDecomposition(IExpression expression, AbstractScope scope) {
        decomposeExpression(expression, 0, true, true, scope);

        // Function calls are decomposed so $0 = return value
        if (expression instanceof FunctionCallExpression)
            return new RegisterExpression(0, scope.getId());
        return expression;
    }
}
