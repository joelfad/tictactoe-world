package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketGameUpdate;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class GameUpdateHandler implements PacketHandler<PacketGameUpdate> {
    private TTTWServerConnection server;
    
    public GameUpdateHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(final PacketGameUpdate packet) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameUpdateHandler.this.server.getFrame().createNewGame(packet);
            }
        });
    }
    
}
