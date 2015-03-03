package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthResult.Result;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

public class CommandDemote implements Command {
    
    @Override
    public String getCommandName() {
        return ":demote";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (!client.getAccount().isAdmin()) {
            client.getServer().sendGlobalBroadcast("<" + client.getAccount().getName() + "> I just did something silly!");
            return;
        } else if (args.length != 2) {
            client.sendMessage("Correct syntax is :demote <player>");
            return;
        }
        
        Account toDemote = client.getServer().getAccountManager().getAccount(args[1]);
        TTTWClientConnection toDemoteConnection = client.getServer().getPlayer(toDemote);
        
        if (toDemote != null) {
            if (toDemote.isAdmin()) {
                if (toDemote != client.getAccount()) {
                    toDemote.setAdmin(false);
                    
                    if (toDemoteConnection != null) {
                        client.getServer().sendPlayerList();
                        
                        try {
                            toDemoteConnection.sendPacket(new PacketAuthResult(Result.OK, toDemote.getName(), false));
                            toDemoteConnection.sendMessage("You have been demoted!");
                        } catch (IOException e) {
                            toDemoteConnection.disconnect("Error sending packet!");
                        }
                    }
                    
                    client.sendMessage("Successfully demoted " + toDemote.getName());
                } else {
                    client.sendMessage("You can't demote yourself!");
                }
            } else {
                client.sendMessage(toDemote.getName() + " is not an administrator!");
            }
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
