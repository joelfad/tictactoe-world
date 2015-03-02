package me.benthomas.tttworld.server.command;

import java.io.IOException;
import java.util.HashMap;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
    }
    
    public static String getCommand(String[] args) {
        StringBuilder cmd = new StringBuilder(args[0]);
        
        for (int i = 1; i < args.length; i++) {
            cmd.append(" " + args[i]);
        }
        
        return cmd.toString();
    }
    
    public static void executeCommand(TTTWClientConnection client, String[] args) throws IOException {
        synchronized (System.out) {
            System.out.println(client.getAddress() + " has executed " + CommandExecutor.getCommand(args));
        }
        
        if (commands.containsKey(args[0])) {
            try {
                commands.get(args[0]).execute(client, args);
            } catch (CommandException e) {
                client.sendPacket(new PacketGlobalChat("Error executing command: " + e.getMessage()));
            }
        } else {
            client.sendPacket(new PacketGlobalChat("Unknown command!"));
        }
    }
    
}
