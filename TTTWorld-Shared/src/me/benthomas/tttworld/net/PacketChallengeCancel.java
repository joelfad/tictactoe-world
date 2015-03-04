package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

/**
 * A packet sent to indicate that a challenge sent by a {@link PacketChallenge}
 * is not longer valid. This packet is only valid from server to client, with
 * the same challenge ID as a previous {@link PacketChallenge}. <strong>Must
 * not</strong> be sent after a {@link PacketChallengeResponse} for a given
 * challenge has already been received.
 * <p>
 * When received by a client, the client <strong>must not</strong> send a
 * {@link PacketChallengeResponse} in response to the given challenge. When
 * receiving a cancellation for a non-existent challenge, the client
 * <strong>may</strong> disconnect or may simply ignore the packet.
 *
 * @author Ben Thomas
 */
public class PacketChallengeCancel extends Packet {
    /**
     * The unique packet identifier used to represent a challenge cancellation
     * packet.
     */
    public static final int PACKET_ID = 16;
    
    private UUID challengeId;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketChallengeCancel() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param challengeId The ID of the challenge which has been cancelled.
     */
    public PacketChallengeCancel(UUID challengeId) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
    }
    
    /**
     * Gets the ID of the challenge which has been cancelled. If this challenge
     * ID does not correspond to a previous {@link PacketChallenge}, the client
     * <strong>may</strong> disconnect or ignore the packet.
     * 
     * @return The ID of the cancelled challenge.
     */
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.challengeId.toString());
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.challengeId = UUID.fromString(o.getString("id"));
    }
}
