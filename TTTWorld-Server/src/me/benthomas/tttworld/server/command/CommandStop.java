package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command which halts the server and disconnects all connected clients. Has a
 * syntax of <code>:stop</code>.
 * <p>
 * Can only be executed by a user who is an administrator on this server.
 *
 * @author Ben Thomas
 */
public class CommandStop implements Command {
    
    @Override
    public String getCommandName() {
        return ":stop";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 1) {
            client.sendMessage("Correct syntax is :stop");
            return;
        }
        
        client.getServer().stop();
    }
    
}
