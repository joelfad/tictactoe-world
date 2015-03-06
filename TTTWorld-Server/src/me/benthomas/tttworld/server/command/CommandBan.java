package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command used to ban particular users from the server. Has a syntax of
 * <code>:ban &lt;player&gt;</code> and will also kick the specified player if
 * they are online.
 * <p>
 * Can only be executed by a user who is an administrator on this server.
 *
 * @author Ben Thomas
 */
public class CommandBan implements Command {
    
    @Override
    public String getCommandName() {
        return ":ban";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 2) {
            client.sendMessage("Correct syntax is :ban <player>");
            return;
        }
        
        TTTWClientConnection toKick = client.getServer().getPlayer(args[1]);
        Account toBan = client.getServer().getAccountManager().getAccount(args[1]);
        
        if (toBan != null) {
            if (toKick != null) {
                toKick.disconnect("Banned by " + client.getAccount().getName());
            }
            
            if (!toBan.isBanned()) {
                toBan.setBanned(true);
                client.sendMessage("Successfully banned " + toBan.getName());
            } else {
                client.sendMessage(toBan.getName() + " is already banned!");
            }
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
