package net.hellomouse.c_interp.interpreter;

public class InterpreterUtil {
    public static String scopeVar(String varName, String scopeId) {
        return scopeId + ":" + varName;
    }
}
