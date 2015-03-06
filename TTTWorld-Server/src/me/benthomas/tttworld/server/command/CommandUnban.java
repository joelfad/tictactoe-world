package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command which unbans the specified user and allows them to reconnect. Has a
 * syntax of <code>:unban &lt;player&gt;</code>.
 * <p>
 * Can only be executed by a user who is an administrator on this server.
 *
 * @author Ben Thomas
 */
public class CommandUnban implements Command {
    
    @Override
    public String getCommandName() {
        return ":unban";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 2) {
            client.sendMessage("Correct syntax is :unban <player>");
            return;
        }
        
        Account toUnban = client.getServer().getAccountManager().getAccount(args[1]);
        
        if (toUnban != null) {
            if (toUnban.isBanned()) {
                toUnban.setBanned(false);
                client.sendMessage("Successfully unbanned " + toUnban.getName());
            } else {
                client.sendMessage(toUnban.getName() + " is not banned!");
            }
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
