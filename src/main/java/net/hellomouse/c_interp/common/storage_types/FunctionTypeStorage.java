package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.FunctionParameter;
import net.hellomouse.c_interp.common.expressions.types.TypeCasting;
import net.hellomouse.c_interp.compiler.Compiler;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.StringJoiner;

public class FunctionTypeStorage extends AbstractTypeStorage {

    public final AbstractTypeStorage returnType;
    public final ArrayList<FunctionParameter> parameters;
    public final String name;
    public final boolean isPrototype;
    public boolean vararg = false;

    private final String id;

    // TODO: implement function storage

    public FunctionTypeStorage(String name, AbstractTypeStorage returnType, ArrayList<FunctionParameter> parameters, boolean isPrototype) {
        this.name = name;
        this.returnType = returnType;
        this.isPrototype = isPrototype;

        ArrayList<FunctionParameter> filteredParameters = new ArrayList<>(parameters.size());
        for (FunctionParameter param : parameters) {
            if (param.type != null && !param.type.isVoid())
                filteredParameters.add(param);
            if (param.vararg)
                this.vararg = true;
        }
        this.parameters = filteredParameters;
        this.id = returnType.getId() + name + filteredParameters + this.vararg + isPrototype;
    }

    // TODO JOIN parmeters, check in gcc
    @Override
    public String getFullName() { return returnType.getFullName() + "()"; }

    public void validiateArguments(ArrayList<IExpression> arguments, Compiler compiler, ParseTree context) {
        boolean varargs = parameters.size() > 0 && vararg;

        // Length of provided arguments matches
        if (!varargs && arguments.size() < parameters.size())
            compiler.error("too few arguments to function '" + name + "'", context);
        else if (!varargs && arguments.size() > parameters.size())
            compiler.error("too many arguments to function '" + name + "'", context);
        else if (varargs && arguments.size() < parameters.size() - 1)
            compiler.error("too few arguments to function '" + name + "'", context);

        // Check type of args
        // If (varargs) last argument is a dummy argument with value ...
        for (int index = 0; index < (varargs ? parameters.size() - 1 : parameters.size()); index++) {
            if (!TypeCasting.areTypesCompatible(arguments.get(index).getType(), parameters.get(index).type, compiler.machine)) {
                compiler.error("incompatible type for argument " + (index + 1) + " of '" + name + "'", context, false);
                compiler.note("expected '" + parameters.get(index).type.getFullName() +
                        "' but argument is of type '" + arguments.get(index).getType().getFullName() + "'", context);
                compiler.silentError();
            }
        }
    }

    @Override
    public String getId() {
        return super.getId() + id;
    }

    @Override
    public int getSize() { return MachineSettings.INVALID_SIZE; } // TODO

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionTypeStorage)) return false;
        FunctionTypeStorage f = (FunctionTypeStorage)o;
        if (!f.name.equals(name) || !f.returnType.equals(returnType) || f.parameters.size() != parameters.size())
            return false;
        for (int i = 0; i < parameters.size(); i++) {
            if (!f.parameters.get(i).equals(parameters.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",");
        for(FunctionParameter param : parameters)
            joiner.add(param.type + "=" + param.name);
        return (isPrototype ? "protofunc " : "func ") + returnType + " " + name + "(" + joiner.toString() + ")";
    }
}
