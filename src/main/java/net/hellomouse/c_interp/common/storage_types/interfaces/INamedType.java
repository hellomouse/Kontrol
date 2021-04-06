package net.hellomouse.c_interp.common.storage_types.interfaces;

/**
 * A type with a name, for example, enums or structs / unions.
 * @author Bowserinator
 */
public interface INamedType {
    /**
     * Get the name of the type, ie "struct myStruct" would
     * return "myStruct"
     * @return name
     */
    String getName();

    /**
     * Get the name of the declaration, ie "struct myStruct" would
     * return "struct myStruct"
     * @return full declaration
     */
    String getFullDeclaration();
}
