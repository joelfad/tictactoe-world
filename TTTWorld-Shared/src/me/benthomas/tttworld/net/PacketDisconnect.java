package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet which signals a client disconnect.
 * <p>
 * When sent from a client to a server, used to signal that the client is about
 * to disconnect from the server. After receiving such a packet from a client,
 * no further packets should be sent.
 * <p>
 * When sent from a server to a client, used to signal that the server is
 * forcibly disconnecting the client. This may occur if the server is shutting
 * down, the user was kicked, or the client violated the TTTW protocol
 * specification.
 *
 * @author Ben Thomas
 */
public class PacketDisconnect extends Packet {
    /**
     * The unique packet identifier used to represent a disconnection packet.
     */
    public static final int PACKET_ID = 0;
    
    private String reason;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketDisconnect() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param reason The reason for which the disconnect is occurring.
     */
    public PacketDisconnect(String reason) {
        super(PACKET_ID);
        
        this.reason = reason;
    }
    
    /**
     * Gets the reason that this disconnection packet is being sent in a
     * human-readable format.
     * 
     * @return The reason for the disconnection.
     */
    public String getReason() {
        return this.reason;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("disconnect_reason", this.reason);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.reason = o.getString("disconnect_reason");
    }
}
