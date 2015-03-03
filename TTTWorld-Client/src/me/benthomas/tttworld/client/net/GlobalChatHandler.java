package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class GlobalChatHandler implements PacketHandler<PacketGlobalChat> {
    private TTTWServerConnection server;
    
    public GlobalChatHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(final PacketGlobalChat packet) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GlobalChatHandler.this.server.getFrame().getChatPane()
                        .setText(GlobalChatHandler.this.server.getFrame().getChatPane().getText() + packet.getMessage() + "\n");
            }
        });
    }
    
}
