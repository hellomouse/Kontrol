package net.hellomouse.c_interp.interpreter;

public class InterpreterUtil {
    public static String scopeVar(String varName, String scopeId) {
        if (scopeId.length() == 0)
            return varName;
        return scopeId + ":" + varName;
    }
}
