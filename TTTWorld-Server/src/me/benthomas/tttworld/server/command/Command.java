package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * Represents a command which can be executed by a client through the use of a
 * chat message prefixed with a colon. Commands should be used for general
 * operations which <strong>do not</strong> involve sending any sensitive data
 * (e.g. passwords) to the server.
 * <p>
 * Commands can have one or more arguments, which are separated by a space. The
 * first argument is interpreted as the command to be executed, with further
 * arguments being for command-specific information.
 *
 * @author Ben Thomas
 */
public interface Command {
    
    /**
     * Gets the name by which this command can be executed, including the
     * prefixed colon required to differentiate commands from regular chat
     * messages.
     * 
     * @return The name of this command, with prefixed colon.
     */
    public String getCommandName();
    
    /**
     * Executes this command in the context of a client with the specified
     * command arguments.
     * 
     * @param client The client that is executing this command.
     * @param args The list of arguments passed to the command. The first
     *            argument should always be the name of the command, with the
     *            prefixed colon. Other arguments are command-specific.
     * 
     * @throws CommandException Indicates that an internal error occurred while
     *             executing the command. Throwing this will notify the user of
     *             the failure, and log the stack trace, but will
     *             <strong>not</strong> disconnect the client.
     * @throws IOException Indicates that an error occurred while relaying
     *             details about the command back to the client. Throwing this
     *             will cause the client to be disconnected.
     */
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException;
}
