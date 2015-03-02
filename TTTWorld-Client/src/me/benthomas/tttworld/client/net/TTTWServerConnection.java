package me.benthomas.tttworld.client.net;

import java.io.IOException;
import java.net.Socket;

import me.benthomas.tttworld.client.ui.MainFrame;
import me.benthomas.tttworld.net.Packet;
import me.benthomas.tttworld.net.PacketKeepAlive;
import me.benthomas.tttworld.net.PacketServerHandshake;
import me.benthomas.tttworld.net.TTTWConnection;

public class TTTWServerConnection extends TTTWConnection {
    private MainFrame frame;
    
    private String hostname;
    
    public TTTWServerConnection(Socket socket, MainFrame frame, String hostname) throws IOException {
        super(socket);
        
        this.frame = frame;
        this.hostname = hostname;
        
        this.setHandler(PacketKeepAlive.class, new KeepAliveHandler(this));
        this.setHandler(PacketServerHandshake.class, new HandshakeHandler(this));
    }
    
    public MainFrame getFrame() {
        return this.frame;
    }
    
    public String getHostName() {
        return this.hostname;
    }

    @Override
    public synchronized void sendPacket(Packet p) {
        try {
            super.sendPacket(p);
        } catch (IOException e) {
            this.disconnect("Failure sending packet!");
        }
    }
}
