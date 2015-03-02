package me.benthomas.tttworld.client.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketKeepAlive;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class KeepAliveHandler implements PacketHandler<PacketKeepAlive> {
    private TTTWServerConnection server;
    
    public KeepAliveHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(PacketKeepAlive packet) throws IOException {
        this.server.sendPacket(new PacketKeepAlive());
    }
    
}
