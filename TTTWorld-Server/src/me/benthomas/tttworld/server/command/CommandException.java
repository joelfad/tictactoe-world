package me.benthomas.tttworld.server.command;

/**
 * Represents a non-fatal internal error that occurred while executing a
 * command.
 *
 * @author Ben Thomas
 */
public class CommandException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new exception with no message and no cause.
     */
    public CommandException() {
        super();
    }
    
    /**
     * Constructs a new exception with the given message.
     * 
     * @param message The reason that this exception has occurred.
     */
    public CommandException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the given cause.
     * 
     * @param cause The exception that caused this exception to be thrown.
     */
    public CommandException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs a new exception with the given message and cause.
     * 
     * @param message The reason that this exception has occurred.
     * @param cause The exception that caused this exception to be thrown.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
