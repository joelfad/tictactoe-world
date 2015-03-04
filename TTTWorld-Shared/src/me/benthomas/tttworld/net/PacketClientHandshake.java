package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet which is sent by a client to begin the handshake process. This
 * packet is only valid when sent from a client to a server. Only one handshake
 * packet may be sent, at the beginning of the connection.
 * <p>
 * When received by a server, the server should check the protocol version
 * indicated by this packet. If the protocol version is unsupported, the server
 * should disconnect the client immediately. If the requested protocol version
 * is supported by the server, the server should reply with a
 * {@link PacketServerHandshake} to continue the handshake process.
 * 
 * @author Ben Thomas
 */
public class PacketClientHandshake extends Packet {
    /**
     * The unique packet identifier used to represent a client handshake packet.
     */
    public static final int PACKET_ID = 1;
    
    private int protocolVersionMajor;
    private int protocolVersionMinor;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketClientHandshake() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param protocolVersionMajor The major version of the protocol that the
     *            client is using.
     * @param protocolVersionMinor The minor version of the protocol that the
     *            client is using.
     */
    public PacketClientHandshake(int protocolVersionMajor, int protocolVersionMinor) {
        super(PACKET_ID);
        
        this.protocolVersionMajor = protocolVersionMajor;
        this.protocolVersionMinor = protocolVersionMinor;
    }
    
    /**
     * Gets the major version of the TTTW protocol being used by the client. In
     * general, major versions are not intercompatible and any discrepancies
     * between major protocol versions should result in an immediate
     * disconnection.
     * 
     * @return The major version of the protocol in use by the client.
     */
    public int getMajorProtocolVersion() {
        return this.protocolVersionMajor;
    }
    
    /**
     * Gets the minor version of the TTTW protocol being used by the client. In
     * general, minor versions are intercompatible with some minor adjustments.
     * 
     * @return The minor version of the protocol in use by the client.
     */
    public int getMinorProtocolVersion() {
        return this.protocolVersionMinor;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        JSONObject v = new JSONObject();
        
        v.put("major", this.protocolVersionMajor);
        v.put("minor", this.protocolVersionMinor);
        
        o.put("protocol_version", v);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.protocolVersionMajor = o.getJSONObject("protocol_version").getInt("major");
        this.protocolVersionMinor = o.getJSONObject("protocol_version").getInt("minor");
    }
}
