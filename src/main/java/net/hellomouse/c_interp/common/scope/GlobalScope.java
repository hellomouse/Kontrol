package net.hellomouse.c_interp.common.scope;

import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The top level scope. Traversing up parents of local scopes should eventually
 * reach the global scope, which only one exists in the compiler
 * @author Bowserinator
 */
public class GlobalScope extends AbstractScope {
    // INamedType name : Arraylist of variable names that use this incomplete type
    public final HashMap<String, ArrayList<String>> incompleteVariables = new HashMap<>();
    // Function name : function type
    public final HashMap<String, FunctionTypeStorage> functionDeclarations = new HashMap<>();

    /** Construct the global scope */
    public GlobalScope() {
        super(null, "");
    }

    @Override
    public FunctionTypeStorage getFunction(String name) {
        return functionDeclarations.get(name);
    }

    /**
     * Add a new struct or union type to the scope. Type CAN be an incomplete
     * struct or union type (allowed at the global scope)
     * @param type StructOrUnionStorage, or IncompleteStructOrUnionStorage
     */
    @Override
    public void addStructOrUnionType(StructOrUnionStorage type) {
        String name = ((INamedType)type).getName();
        customStructOrUnionTypes.put(name, type);

        String typeName = ((INamedType)type).getName();
        if (incompleteVariables.containsKey(typeName)) {
            for (String varName : incompleteVariables.get(typeName))
                variables.get(varName).setType(type);
            incompleteVariables.remove(typeName);
        }
    }
}
