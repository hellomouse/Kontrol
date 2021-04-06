package net.hellomouse.c_interp.common.expressions;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class FunctionCallExpression implements IExpression {
    public final String funcName;
    public final ArrayList<IExpression> arguments;
    public final FunctionTypeStorage funcType;
    public final HashMap<String, IExpression> argumentMap = new HashMap<>();

    public FunctionCallExpression(String funcName, ArrayList<IExpression> arguments, FunctionTypeStorage funcType) {
        this.funcName = funcName;
        this.arguments = arguments;
        this.funcType = funcType;

        for (int i = 0; i < funcType.parameters.size(); i++)
            argumentMap.put(funcType.parameters.get(i).name, arguments.get(i));
    }

    public AbstractTypeStorage getType() {
        return funcType.returnType;
    }


    @Override
    public IRuntimeConstant runtimeEval(InterpreterState state) {
        // TODO: somehow return struct too


        state.interpreter.listeners.onFunctionCall(funcName, argumentMap);
        state.callFunction(funcName, arguments);

        return null;
    }

    public String toString() {
        StringJoiner args = new StringJoiner(",");
        for (IExpression arg : arguments)
            args.add(arg.toString());
        return funcName + "(" + args.toString() + ")";
    }
}
