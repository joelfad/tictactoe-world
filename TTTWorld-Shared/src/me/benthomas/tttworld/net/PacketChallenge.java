package me.benthomas.tttworld.net;

import java.util.UUID;

import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;

import org.json.JSONObject;

/**
 * A packet sent by the server to notify a client that they have been challenged
 * to a game of tic-tac-toe by another user. This packet is only valid from
 * server to client, and can be sent at any time after the handshake is
 * completed. May be followed by a {@link PacketChallengeCancel} if the
 * challenge is withdrawn for any reason.
 * <p>
 * When received by a client, the client should prompt the user for what to do.
 * The client should reply with a {@link PacketChallengeResponse} when the end
 * user decides whether to accept the challenge.
 *
 * @author Ben Thomas
 */
public class PacketChallenge extends Packet {
    /**
     * The unique packet identifier used to represent a challenge packet.
     */
    public static final int PACKET_ID = 11;
    
    private UUID challengeId;
    private PlayerInfo sender;
    private long timeout;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketChallenge() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param challengeId The unique identifier associated with this specific
     *            challenge.
     * @param sender Information about the user who sent the challenge.
     * @param timeout The amount of time (in milliseconds) that the challenge is
     *            valid for.
     */
    public PacketChallenge(UUID challengeId, PlayerInfo sender, long timeout) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
        this.sender = sender;
        this.timeout = timeout;
    }
    
    /**
     * Gets the unique identifier of the challenge that this packet is notifying
     * of.
     * 
     * @return The unique ID of this challenge.
     */
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
    /**
     * Gets information regarding the user who sent this challenge.
     * 
     * @return Information regarding the user who sent this challenge.
     */
    public PlayerInfo getSender() {
        return this.sender;
    }
    
    /**
     * Gets the time (in milliseconds) that the challenge is valid for. When
     * this time lapses, the challenge is no longer valid and a
     * {@link PacketChallengeCancel} should be received.
     * 
     * @return The timeout (in milliseconds) of this challenge.
     */
    public long getTimeout() {
        return this.timeout;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.challengeId.toString());
        o.put("sender", this.sender.write());
        o.put("timeout", this.timeout);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.challengeId = UUID.fromString(o.getString("id"));
        this.sender = new PlayerInfo(o.getJSONObject("sender"));
        this.timeout = o.getLong("timeout");
    }
}
