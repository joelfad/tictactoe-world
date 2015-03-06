package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command which kicks the specified user from the server. Has a syntax of
 * <code>:kick &lt;player&gt;</code> and does not prevent the client from
 * reconnecting in any way.
 * <p>
 * Can only be executed by a user who is an administrator on this server.
 *
 * @author Ben Thomas
 */
public class CommandKick implements Command {
    
    @Override
    public String getCommandName() {
        return ":kick";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 2) {
            client.sendMessage("Correct syntax is :kick <player>");
            return;
        }
        
        TTTWClientConnection toKick = client.getServer().getPlayer(args[1]);
        
        if (toKick != null) {
            toKick.disconnect("Kicked by " + client.getAccount().getName());
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
