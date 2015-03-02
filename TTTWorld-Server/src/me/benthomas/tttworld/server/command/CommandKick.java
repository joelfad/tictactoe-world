package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
            client.sendPacket(new PacketGlobalChat("Correct syntax is :kick <player>"));
            return;
        }
        
        TTTWClientConnection toKick = client.getServer().getPlayer(args[1]);
        
        if (toKick != null) {
            toKick.disconnect("Kicked by " + client.getAccount().getName());
        } else {
            client.sendPacket(new PacketGlobalChat("Could not find player " + args[1]));
        }
    }
    
}
