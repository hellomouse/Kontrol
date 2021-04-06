package net.hellomouse.c_interp.interpreter;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.Variable;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Handles listening events
 * @author Bowserinator
 */
public class InterpreterListenerHandler {
    private final ArrayList<InterpreterListener> listeners = new ArrayList<>();

    /** Construct a listener handler */
    public InterpreterListenerHandler() {}

    /**
     * Attach a listener
     * @param listener Listener to attach
     */
    public void attach(InterpreterListener listener) {
        listeners.add(listener);
    }

    /**
     * Call this when a variable extension is referenced
     * in a variable extension
     * @param variable Variable that was referenced
     */
    public void onVariableAccess(Variable variable) {
        for (InterpreterListener listener : listeners)
            if (listener.onVariableAccess != null)
                listener.onVariableAccess.apply(variable);
    }

    /**
     * Call this when a variable is changed either through
     * declaration or assignment
     * @param variable Variable that changed, post-change
     */
    public void onVariableChange(Variable variable) {
        for (InterpreterListener listener : listeners)
            if (listener.onVariableChange != null)
                listener.onVariableChange.apply(variable);
    }

    /**
     * Call this when a variable is declared
     * @param variable Variable that was declared
     */
    public void onVariableDeclaration(Variable variable) {
        for (InterpreterListener listener : listeners)
            if (listener.onVariableDeclaration != null)
                listener.onVariableDeclaration.apply(variable);
    }

    /**
     * Call this when a function is called
     * @param name Name of function called
     * @param parameters Parameters passed
     */
    public void onFunctionCall(String name, HashMap<String, IExpression> parameters) {
        for (InterpreterListener listener : listeners)
            if (listener.onFunctionCall != null)
                listener.onFunctionCall.apply(name, parameters);
    }

    /**
     * Call this when entering a function scope
     * @param name Name of function entered
     */
    public void onEnterFunction(String name) {
        for (InterpreterListener listener : listeners)
            if (listener.onEnterFunction != null)
                listener.onEnterFunction.apply(name);
    }

    /**
     * Call this when exiting a function scope
     * @param name Name of function just exited
     */
    public void onExitFunction(String name) {
        for (InterpreterListener listener : listeners)
            if (listener.onExitFunction != null)
                listener.onExitFunction.apply(name);
    }

    /**
     * A listener instance
     * @author Bowserinator
     */
    public static class InterpreterListener implements EventListener {
        public Function<Variable, Void> onVariableAccess;
        public Function<Variable, Void> onVariableChange;
        public Function<Variable, Void> onVariableDeclaration;
        public BiFunction<String, HashMap<String, IExpression>, Void> onFunctionCall;
        public Function<String, Void> onEnterFunction;
        public Function<String, Void> onExitFunction;

        /**
         * Set the function on variable access
         * @param callback Lambda variable -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onVariableAccess(Function<Variable, Void> callback) {
            this.onVariableAccess = callback;
            return this;
        }

        /**
         * Set the function on variable change
         * @param callback Lambda variable -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onVariableChange(Function<Variable, Void> callback) {
            this.onVariableChange = callback;
            return this;
        }

        /**
         * Set the function on declaration
         * @param callback Lambda variable -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onVariableDeclaration(Function<Variable, Void> callback) {
            this.onVariableDeclaration = callback;
            return this;
        }

        /**
         * Set the function on function call
         * @param callback Lambda (name, parameters) -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onFunctionCall(BiFunction<String, HashMap<String, IExpression>, Void> callback) {
            this.onFunctionCall = callback;
            return this;
        }

        /**
         * Set the function on entering a function
         * @param callback Lambda name -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onEnterFunction(Function<String, Void> callback) {
            this.onEnterFunction = callback;
            return this;
        }

        /**
         * Set the function on exiting a function. Name is the name of the
         * function that was exited.
         * @param callback Lambda name -> { ..., return null; }
         * @return this
         */
        public InterpreterListener onExitFunction(Function<String, Void> callback) {
            this.onExitFunction = callback;
            return this;
        }
    }
}
