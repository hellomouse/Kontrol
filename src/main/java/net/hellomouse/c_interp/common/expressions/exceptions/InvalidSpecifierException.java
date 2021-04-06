package net.hellomouse.c_interp.common.expressions.exceptions;

/**
 * Thrown when a specifier is invalid during specifier checks. Caught
 * in DefinitionHandler to get the error message for the compiler
 * @author Bowserinator
 */
public class InvalidSpecifierException extends RuntimeException {
    /**
     * Construct an InvalidSpecifierException
     * @param errorMessage Error message, will be displayed in compiler.error during
     *                     definition handling
     */
    public InvalidSpecifierException(String errorMessage) {
        super(errorMessage);
    }
}
