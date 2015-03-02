package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
            client.sendPacket(new PacketGlobalChat("Correct syntax is :ban <player>"));
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
                client.sendPacket(new PacketGlobalChat("Successfully banned " + toBan.getName()));
            } else {
                client.sendPacket(new PacketGlobalChat(toBan.getName() + " is already banned!"));
            }
        } else {
            client.sendPacket(new PacketGlobalChat("Could not find player " + args[1]));
        }
    }
    
}
