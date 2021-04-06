package net.hellomouse.c_interp.common.scope;

import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.EnumStorage;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import org.antlr.v4.runtime.misc.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Abstract scope to evaluate variables in. Used at compile time
 * @author Bowserinator
 */
public abstract class AbstractScope {
    // Enumerator value name => int
    public final HashMap<String, ConstantValue> enumValues = new HashMap<>();
    // Enum name => enum storage type
    public final HashMap<String, EnumStorage> customEnumTypes = new HashMap<>();
    // Custom type name from typedef => type
    public final HashMap<String, AbstractTypeStorage> typedefTypes = new HashMap<>();

    // Struct name => struct storage type (complete types only)
    public final HashMap<String, StructOrUnionStorage> customStructOrUnionTypes = new HashMap<>();

    // Variable name => variable
    public final HashMap<String, Variable> variables = new HashMap<>();

    public final AbstractScope parent;
    public final String id;
    public final int parentCount;
    private int childCount = 0;

    /**
     * Construct a scope instance
     * @param parent Parent of the scope, should be null if top level (global) scope
     * @param id Id for this current scope, will be contacted with parent id. Ids are connected
     *           between scope with a '.', ie "global" <-- "local" becomes "global.local"
     *           A parent with an empty string as its id will be treated as if it were null
     *           for the purposes of id generation
     */
    public AbstractScope(@Nullable AbstractScope parent, String id) {
        this.parent = parent;
        this.id = (parent == null || parent.id.length() == 0) ?
                id : parent.id + "." + id;
        this.parentCount = parent == null ? 0 : parent.parentCount + 1;

        if (parent != null)
            this.incrementChildCount();
    }

    /**
     * Add a new struct or union type to the scope. Whether type can be an
     * IncompleteStructOrUnionStorage is up to the implementation
     * @param type A struct or union type
     */
    public abstract void addStructOrUnionType(StructOrUnionStorage type);

    /**
     * Try to get a variable by name, will search this scope
     * and all parent scopes.
     * @param name Name of the variable to retrieve
     * @return IVariable, or null if not found
     */
    public Variable getVariable(String name) {
        return getT(name, "variables");
    }

    /**
     * Try to get a variable by name, will search this scope
     * and all parent scopes. Returns a pair of variable, scope.
     * @param name Name of variable
     * @return Pair of variable and scope it belongs to
     */
    public Pair<Variable, AbstractScope> getVariableAndScope(String name) {
        AbstractScope scope = this;
        while (scope != null) {
            if (scope.variables.containsKey(name))
                return new Pair<>(scope.variables.get(name), scope);
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Try to get an enum type by name, will search this scope
     * and all parent scopes
     * @param name Name of the enum type
     * @return EnumStorage, or null if not found
     */
    public EnumStorage getEnumType(String name) {
        return getT(name, "customEnumTypes");
    }

    /**
     * Try to get an enum value by name, ie enum {A, B, C}, getEnumValue("A") will return
     * the value of A (0). Will search this scope and all parent scopes.
     * @param name Name of the enum value
     * @return Int CompileTimeValue, or null if not found
     */
    public ConstantValue getEnumValue(String name) {
        return getT(name, "enumValues");
    }

    /**
     * Try to get a typedefed type by name, ie typedef int xint, getTypedef("xint") would return
     * a type of int. Will search this scope and all parent scopes.
     * @param name Custom type name
     * @return AbstractTypeStorage, or null if not found
     */
    public AbstractTypeStorage getTypedef(String name) { return getT(name, "typedefTypes"); }

    /**
     * Get a struct or union type by name. Will search this scope and all parent scopes.
     * @param name Name of the struct or union type
     * @return StructOrUnionStorage, or null if not found
     */
    public StructOrUnionStorage getStructOrUnion(String name) { return getT(name, "customStructOrUnionTypes"); }

    /**
     * Get a function by name. Currently it always gets the function
     * from the global scope.
     * @param name Name of the function
     * @return FunctionTypeStorage, or null if not found
     */
    public abstract FunctionTypeStorage getFunction(String name);

    /**
     * Private helper method to get a key from a map, returns null
     * if the key doesn't exist. Will search this scope and all
     * parent scopes.
     * @param name Name of the map to search
     * @param key Key to search
     * @param <T> Type, not needed because of implicit cast
     * @return T, or null if not found
     */
    private <T> T getT(String name, String key) {
        AbstractScope scope = this;
        while (scope != null) {
            HashMap<String, T> map = scope.getMapFromName(key);
            if (map.containsKey(name))
                return map.get(name);
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Returns a map given its name
     * @param key Name of the map
     * @param <T> Type, not needed because of implicit cast
     * @return HashMap of String, T
     */
    @SuppressWarnings("unchecked")
    private <T> HashMap<String, T> getMapFromName(String key) {
        switch(key) {
            case "variables" -> { return (HashMap<String, T>)variables; }
            case "customEnumTypes" -> { return (HashMap<String, T>)customEnumTypes; }
            case "enumValues" -> { return (HashMap<String, T>)enumValues; }
            case "typedefTypes" -> { return (HashMap<String, T>)typedefTypes; }
            case "customStructOrUnionTypes" -> { return (HashMap<String, T>)customStructOrUnionTypes; }
        }
        throw new IllegalStateException("Unknown map name " + key);
    }

    /**
     * Get a unique ID for this scope
     * @return Unique string
     */
    public String getId() {
        return id;
    }

    /** Increment the child count */
    public void incrementChildCount() { childCount++; }

    /**
     * How many children does this scope have currently
     * @return Child count
     */
    public int getChildCount() { return childCount; }
}
