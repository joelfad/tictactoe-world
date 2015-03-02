package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.command.CommandExecutor;

public class GlobalChatHandler implements PacketHandler<PacketGlobalChat> {
    private TTTWClientConnection client;
    
    public GlobalChatHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketGlobalChat packet) throws IOException {
        if (packet.getMessage().startsWith(":")) {
            CommandExecutor.executeCommand(this.client, packet.getMessage().split(" "));
        } else {
            this.client.getServer().sendGlobalBroadcast("<" + this.client.getAccount().getName() + "> " + packet.getMessage());
        }
    }
    
}
