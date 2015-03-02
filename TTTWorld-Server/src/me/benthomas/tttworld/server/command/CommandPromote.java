package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthResult.Result;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
            client.sendPacket(new PacketGlobalChat("Correct syntax is :promote <player>"));
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
                        toPromoteConnection.sendPacket(new PacketGlobalChat("You have been promoted!"));
                    } catch (IOException e) {
                        toPromoteConnection.disconnect("Error sending packet!");
                    }
                }
                
                client.sendPacket(new PacketGlobalChat("Successfully promoted " + toPromote.getName()));
            } else {
                client.sendPacket(new PacketGlobalChat(toPromote.getName() + " is already an administrator!"));
            }
        } else {
            client.sendPacket(new PacketGlobalChat("Could not find player " + args[1]));
        }
    }
    
}
