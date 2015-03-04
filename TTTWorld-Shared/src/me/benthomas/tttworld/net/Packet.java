package me.benthomas.tttworld.net;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a TTTW protocol compliant packet that can be sent to a
 * TTTW-compliant endpoint.
 * <p>
 * Note that not all packets are considered to be valid. For instance, some
 * types of packets are only allowed to be sent from a client to a
 * TTTW-compliant server and not the other way around. In addition, some packets
 * have additional requirements that must be met before they can be sent. As an
 * example, most packets cannot be sent to a TTTW-compliant server until a
 * client has performed proper authentication.
 * <p>
 * Any restrictions on when and how a packet may be sent are documented on the
 * Javadoc for that specific packet.
 * 
 * @author Ben Thomas
 */
public abstract class Packet {
    private static HashMap<Integer, Class<? extends Packet>> packetTypes = new HashMap<Integer, Class<? extends Packet>>();
    
    static {
        packetTypes.put(PacketKeepAlive.PACKET_ID, PacketKeepAlive.class);
        packetTypes.put(PacketDisconnect.PACKET_ID, PacketDisconnect.class);
        
        packetTypes.put(PacketClientHandshake.PACKET_ID, PacketClientHandshake.class);
        packetTypes.put(PacketServerHandshake.PACKET_ID, PacketServerHandshake.class);
        packetTypes.put(PacketStartEncrypt.PACKET_ID, PacketStartEncrypt.class);
        packetTypes.put(PacketAuthenticate.PACKET_ID, PacketAuthenticate.class);
        packetTypes.put(PacketRegister.PACKET_ID, PacketRegister.class);
        packetTypes.put(PacketAuthResult.PACKET_ID, PacketAuthResult.class);
        
        packetTypes.put(PacketPasswordChange.PACKET_ID, PacketPasswordChange.class);
        packetTypes.put(PacketPasswordChangeResult.PACKET_ID, PacketPasswordChangeResult.class);
        
        packetTypes.put(PacketGlobalChat.PACKET_ID, PacketGlobalChat.class);
        packetTypes.put(PacketGlobalPlayerList.PACKET_ID, PacketGlobalPlayerList.class);
        
        packetTypes.put(PacketChallenge.PACKET_ID, PacketChallenge.class);
        packetTypes.put(PacketChallengeResponse.PACKET_ID, PacketChallengeResponse.class);
        packetTypes.put(PacketChallengeCancel.PACKET_ID, PacketChallengeCancel.class);
        
        packetTypes.put(PacketGameUpdate.PACKET_ID, PacketGameUpdate.class);
        packetTypes.put(PacketGameMove.PACKET_ID, PacketGameMove.class);
        packetTypes.put(PacketGameOver.PACKET_ID, PacketGameOver.class);
    }
    
    /**
     * Reads a TTTW packet from the given JSON payload. Note that the payload
     * <strong>must</strong> be processed first to remove any encryption or
     * compression that may have been applied.
     * 
     * @param payload The JSON-encoded packet to be read. Must be processed and
     *            should contain <strong>only</strong> the JSON-encoded part of
     *            the packet.
     * @return The packet that has been decoded.
     * 
     * @throws IOException The packet was malformed or otherwise could not be
     *             decoded properly.
     */
    public static Packet readPacket(String payload) throws IOException {
        try {
            JSONObject json = new JSONObject(payload);
            Class<? extends Packet> packetType = packetTypes.get(json.getInt("packet_id"));
            
            if (packetType == null) {
                throw new IOException("Invalid packet");
            }
            
            Packet p = packetType.newInstance();
            p.read(json);
            
            return p;
        } catch (JSONException | IllegalArgumentException e) {
            throw new IOException("Invalid packet", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * The identifier used to identify this specific type of packet. It is
     * unique to a given type of packet and is used to determine how a packet
     * should be decoded.
     */
    public final int packetId;
    private long timestamp;
    
    /**
     * Create a blank packet with the given identifier. The identifier should be
     * provided by the implementing class and should <strong>not</strong> be
     * user-definable.
     * 
     * @param packetId The identifier of the type of packet represented by this
     *            instance.
     */
    public Packet(int packetId) {
        this.packetId = packetId;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the timestamp of this packet. The timestamp of a packet is the value
     * of {@link System#currentTimeMillis()} on the machine that sent the packet
     * when the packet was sent.
     * <p>
     * Packets with timestamps which deviate from the receiving machine's clock
     * by more than {@link TTTWConnection#MAX_CLOCK_DEVIATION} should be
     * rejected to prevent replay attacks.
     * 
     * @return The timestamp encoded on this packet.
     */
    public final long getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * Encodes this packet into a {@link JSONObject} which can be sent to
     * another TTTW-compliant endpoint.
     * 
     * @return The JSON representation of this packet.
     */
    public JSONObject write() {
        JSONObject o = new JSONObject();
        
        o.put("packet_id", this.packetId);
        o.put("timestamp", this.timestamp);
        
        return o;
    }
    
    /**
     * Reads values into this packet instance based on an encoded
     * {@link JSONObject} which has been received.
     * 
     * @param o The encoded representation of the packet.
     */
    public void read(JSONObject o) {
        this.timestamp = o.getLong("timestamp");
    }
}
