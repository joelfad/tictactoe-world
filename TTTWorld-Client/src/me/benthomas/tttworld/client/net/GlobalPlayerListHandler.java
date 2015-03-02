package me.benthomas.tttworld.client.net;

import java.io.IOException;
import java.util.Collections;

import javax.swing.SwingUtilities;

import me.benthomas.tttworld.client.ui.MainFrame;
import me.benthomas.tttworld.net.PacketGlobalPlayerList;
import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class GlobalPlayerListHandler implements PacketHandler<PacketGlobalPlayerList> {
    private TTTWServerConnection server;
    
    public GlobalPlayerListHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(PacketGlobalPlayerList packet) throws IOException {
        Collections.sort(packet.getPlayers());
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame f = GlobalPlayerListHandler.this.server.getFrame();
                
                f.getPlayerListModel().clear();
                
                for (PlayerInfo player : packet.getPlayers()) {
                    f.getPlayerListModel().addElement(player);
                }
            }
        });
    }
    
}
