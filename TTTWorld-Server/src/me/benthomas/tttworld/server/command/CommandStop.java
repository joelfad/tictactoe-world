package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

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
            client.sendPacket(new PacketGlobalChat("Correct syntax is :stop"));
            return;
        }
        
        client.getServer().stop();
    }
    
}
