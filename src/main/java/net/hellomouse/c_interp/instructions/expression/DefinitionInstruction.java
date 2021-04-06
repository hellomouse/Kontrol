package net.hellomouse.c_interp.instructions.expression;

import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;
import net.hellomouse.c_interp.interpreter.InterpreterUtil;

public class DefinitionInstruction extends AbstractInstruction {
    public final Variable definition;
    public final String scopeId;

    public DefinitionInstruction(Variable definition, String scopeId) {
        this.definition = definition;
        this.scopeId = scopeId;
    }

    public String toASMLine() {
        // TODO: move toString
        return getInstrName() + " " + definition.getType() + " " + definition.getName() + " = " + definition.getValue();
    }

    public String getInstrName() {
        return "DEFINE";
    }

    public void interpret(InterpreterState state) {
        state.log.definition("Defining " + definition + " at " + state.getStackPointer());

        // Stack memory stuff

        Variable definition = this.definition.copy();

        // TODO: explain
        if (!(definition.getType() instanceof FunctionTypeStorage))
            definition.setName(InterpreterUtil.scopeVar(definition.getName(), scopeId));

        state.incrementStackPointer(definition.getType().getSize());
        state.addVariable(definition);

        state.interpreter.listeners.onVariableDeclaration(definition);
        state.interpreter.listeners.onVariableChange(definition);

        if (definition.getValue() != null) {
            IRuntimeConstant value = definition.getValue().runtimeEval(state);
            value.postOperation(definition.getType());
            state.getCurrentScope().getVariable(definition.getName()).setValue(value);
        }
    }
}
