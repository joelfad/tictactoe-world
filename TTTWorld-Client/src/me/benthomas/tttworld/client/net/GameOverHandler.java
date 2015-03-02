package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketGameOver;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class GameOverHandler implements PacketHandler<PacketGameOver> {
    private TTTWServerConnection server;
    
    public GameOverHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(PacketGameOver packet) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameOverHandler.this.server.getFrame().handleGameOver(packet);
            }
        });
    }
    
}
