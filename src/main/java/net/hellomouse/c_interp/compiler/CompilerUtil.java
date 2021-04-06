package net.hellomouse.c_interp.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

/**
 * Compile time utilities
 * @author Bowserinator
 */
public class CompilerUtil {
    /**
     * Get an array list of all parseTrees that match a given predicate. The root node will be included
     * in the search if it matches the predicate.
     *
     * @param root Root node of the tree to search
     * @param predicate A function that accepts a parseTree and returns whether it matches
     * @param onlyTopLevel If a parseTree matches the predicate and this is true its children will
     *                     not be searched.
     * @return ArrayList of all matching trees
     */
    public static ArrayList<ParseTree> getTreesByRule(ParseTree root, Function<ParseTree, Boolean> predicate, boolean onlyTopLevel) {
        ArrayList<ParseTree> matches = new ArrayList<>();
        Queue<ParseTree> queue = new LinkedList<>();
        queue.add(root);

        while (queue.size() > 0) {
            ParseTree context = queue.remove();

            boolean result = predicate.apply(context);
            if (result) matches.add(context);
            if (result && onlyTopLevel) continue;

            for (int i = 0; i < context.getChildCount(); i++)
                queue.add(context.getChild(i));
        }
        Collections.reverse(matches);
        return matches;
    }

    /**
     * Returns the line and char number of a parseTree as a pair
     * @param node Node to get line and char pos of
     * @return Pair of Line number, Char Number
     */
    public static Pair<Integer, Integer> getLineCharNumber(ParseTree node) {
        Token token;
        if (node instanceof TerminalNodeImpl)
            token = ((TerminalNodeImpl)node).symbol;
        else if (node instanceof ParserRuleContext)
            token = ((ParserRuleContext)node).getStart();
        else
            throw new IllegalStateException("Expected TerminalNodeImpl or ParserRuleContext, got " + node.getClass());

        int line = token.getLine();
        int charPos = token.getCharPositionInLine();

        return new Pair<>(line, charPos);
    }
}
