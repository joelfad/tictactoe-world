package me.benthomas.tttworld.net;

/**
 * A special type of packet which is used to signal that the connection between
 * two TTTW-compliant endpoints is still alive. A server will send this packet
 * if a client has not sent any other packets for a significant period of time.
 * <p>
 * When received by a server, the server should not perform any action. This
 * packet is only used to update the time of the last received packet.
 * <p>
 * When received by a client, the client <strong>must</strong> reply with a
 * keep-alive packet of its own, or risk being disconnected due to a connection
 * timeout.
 *
 * @author Ben Thomas
 */
public class PacketKeepAlive extends Packet {
    /**
     * The unique packet identifier used to represent a keep-alive packet.
     */
    public static final int PACKET_ID = -1;
    
    /**
     * Creates a new keep-alive packet which may be sent or received. Due to the
     * fact that keep-alive packets contain no additional information, this
     * constructor is suitable for both cases.
     */
    public PacketKeepAlive() {
        super(PACKET_ID);
    }
}
