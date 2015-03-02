package me.benthomas.tttworld.net;

public class PacketKeepAlive extends Packet {
    public static final int PACKET_ID = -1;
    
    public PacketKeepAlive() {
        super(PACKET_ID);
    }
}
