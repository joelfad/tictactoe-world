package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthResult.Result;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command used to promote a specified user from a regular user to an
 * administrator. Has a syntax of <code>:promote &lt;player&gt;</code>.
 * <p>
 * Can only be executed by a user who is an administrator on this server.
 *
 * @author Ben Thomas
 */
public class CommandPromote implements Command {
    
    @Override
    public String getCommandName() {
        return ":promote";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 2) {
            client.sendMessage("Correct syntax is :promote <player>");
            return;
        }
        
        Account toPromote = client.getServer().getAccountManager().getAccount(args[1]);
        TTTWClientConnection toPromoteConnection = client.getServer().getPlayer(toPromote);
        
        if (toPromote != null) {
            if (!toPromote.isAdmin()) {
                toPromote.setAdmin(true);
                
                if (toPromoteConnection != null) {
                    client.getServer().sendPlayerList();
                    
                    try {
                        toPromoteConnection.sendPacket(new PacketAuthResult(Result.OK, toPromote.getName(), true));
                        toPromoteConnection.sendMessage("You have been promoted!");
                    } catch (IOException e) {
                        toPromoteConnection.disconnect("Error sending packet!");
                    }
                }
                
                client.sendMessage("Successfully promoted " + toPromote.getName());
            } else {
                client.sendMessage(toPromote.getName() + " is already an administrator!");
            }
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
