package me.benthomas.tttworld.client.net;

import java.io.IOException;

public class PacketHandleThread extends Thread {
    private static final long PACKET_PERIOD = 50;
    private static final long PACKET_TIMEOUT = 30000;
    
    private TTTWServerConnection server;
    
    public PacketHandleThread(TTTWServerConnection server) {
        super("Packet Handling Thread");
        
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            while (this.server.isAlive()) {
                Thread.sleep(PacketHandleThread.PACKET_PERIOD);
                
                try {
                    if (this.server.isAlive() && this.server.isPacketWaiting()) {
                        this.server.handleNextPacket();
                    } else if (this.server.getTimeSinceLastPacket() > PacketHandleThread.PACKET_TIMEOUT) {
                        this.server.disconnect("Connection timed out!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.server.disconnect("Error reading packet!");
                }
            }
        } catch (InterruptedException e) {
            // Shut down the thread
        }
    }
    
}
