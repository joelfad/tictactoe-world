package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketChallenge;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class ChallengeHandler implements PacketHandler<PacketChallenge> {
    private TTTWServerConnection server;
    
    public ChallengeHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(final PacketChallenge packet) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChallengeHandler.this.server.getFrame().displayChallengeDialog(packet);
            }
        });
    }
    
}
