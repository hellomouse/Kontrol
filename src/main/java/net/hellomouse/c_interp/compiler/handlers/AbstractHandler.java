package net.hellomouse.c_interp.compiler.handlers;

import net.hellomouse.c_interp.compiler.Compiler;

/**
 * Abstract handler for a type of Context
 * @author Bowserinator
 */
public abstract class AbstractHandler {
    protected final Compiler compiler;

    /**
     * Construct handler for a given compiler. The handler should be public final
     * in the Compiler to allow free access from other handlers
     * @param compiler Compiler instance
     */
    public AbstractHandler(Compiler compiler) { this.compiler = compiler; }
}
