package me.benthomas.tttworld.server.command;

import java.io.IOException;
import java.util.HashMap;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A class with static methods capable of executing commands on behalf of a
 * user.
 *
 * @author Ben Thomas
 */
public class CommandExecutor {
    private static HashMap<String, Command> commands = new HashMap<String, Command>();
    
    private static void registerCommand(Command c) {
        CommandExecutor.commands.put(c.getCommandName(), c);
    }
    
    static {
        CommandExecutor.registerCommand(new CommandKick());
        CommandExecutor.registerCommand(new CommandBan());
        CommandExecutor.registerCommand(new CommandUnban());
        CommandExecutor.registerCommand(new CommandPromote());
        CommandExecutor.registerCommand(new CommandDemote());
        CommandExecutor.registerCommand(new CommandStop());
        CommandExecutor.registerCommand(new CommandChallenge());
        CommandExecutor.registerCommand(new CommandPlayAI());
    }
    
    /**
     * Finds the full command line that would have been sent by a client to
     * result in the provided arguments being passed.
     * 
     * @param args The command arguments that were sent by the client, including
     *            the command name prefixed with a colon as the first argument.
     * @return The full command line that would have been sent by the client.
     */
    public static String getCommand(String[] args) {
        StringBuilder cmd = new StringBuilder(args[0]);
        
        for (int i = 1; i < args.length; i++) {
            cmd.append(" " + args[i]);
        }
        
        return cmd.toString();
    }
    
    /**
     * Executes the given command on behalf of the provided client.
     * 
     * @param client The client that is executing this command.
     * @param args The arguments that the client has used, including the command
     *            name prefixed with a colon as the first argument.
     * @throws IOException Thrown to indicate that a serious error occurred
     *             while sending the results of the command to the user.
     *             Throwing this should cause the user to be disconnected.
     */
    public static void executeCommand(TTTWClientConnection client, String[] args) throws IOException {
        synchronized (System.out) {
            System.out.println(client.getAddress() + " has executed " + CommandExecutor.getCommand(args));
        }
        
        if (commands.containsKey(args[0])) {
            try {
                commands.get(args[0]).execute(client, args);
            } catch (CommandException e) {
                client.sendMessage("Error executing command: " + e.getMessage());
            }
        } else {
            client.sendMessage("Unknown command!");
        }
    }
    
}
