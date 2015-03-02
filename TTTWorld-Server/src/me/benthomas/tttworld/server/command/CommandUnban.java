package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
            client.sendPacket(new PacketGlobalChat("Correct syntax is :unban <player>"));
            return;
        }
        
        Account toUnban = client.getServer().getAccountManager().getAccount(args[1]);
        
        if (toUnban != null) {
            if (toUnban.isBanned()) {
                toUnban.setBanned(false);
                client.sendPacket(new PacketGlobalChat("Successfully unbanned " + toUnban.getName()));
            } else {
                client.sendPacket(new PacketGlobalChat(toUnban.getName() + " is not banned!"));
            }
        } else {
            client.sendPacket(new PacketGlobalChat("Could not find player " + args[1]));
        }
    }
    
}
