package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

/**
 * A packet sent by a client to indicate a response to a challenge in response
 * to a {@link PacketChallenge}. This packet is only valid from client to
 * server. This packet should be sent after receiving a {@link PacketChallenge}
 * for a given challenge, and <strong>must not</strong> be sent after receiving
 * a {@link PacketChallengeCancel} for the challenge.
 * <p>
 * When received by a server, the server should find the requested challenge and
 * perform the requested action. An invalid challenge ID <strong>may</strong>
 * result in disconnection or may simply be ignored.
 *
 * @author Ben Thomas
 */
public class PacketChallengeResponse extends Packet {
    /**
     * The unique packet identifier used to represent a challenge response
     * packet.
     */
    public static final int PACKET_ID = 12;
    
    private UUID challengeId;
    private Response response;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketChallengeResponse() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param challengeId The ID of the challenge to which this packet is a
     *            response.
     * @param response The response to the challenge.
     */
    public PacketChallengeResponse(UUID challengeId, Response response) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
        this.response = response;
    }
    
    /**
     * Gets the ID of the challenge to which this packet is responding. This
     * <strong>must</strong> be the same as the ID received by the client in the
     * {@link PacketChallenge} to which this is a response.
     * 
     * @return The ID of the challenge to which this is a response.
     */
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
    /**
     * The response to the challenge.
     * 
     * @return The response to the challenge.
     */
    public Response getResponse() {
        return this.response;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.challengeId.toString());
        o.put("response", response.identifier);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.challengeId = UUID.fromString(o.getString("id"));
        this.response = Response.getByIdentifier(o.getString("response"));
    }
    
    /**
     * A value indicating a client's response to a challenge. A response of
     * {@link Response#ACCEPT} should accept the challenge and initiate the
     * game, and a response of {@link Response#REJECT} <strong>must not</strong>
     * start a game.
     *
     * @author Ben Thomas
     */
    public enum Response {
        /**
         * A response indicating that the client wishes to accept the proposed
         * challenge and that the server should initiate the game.
         */
        ACCEPT("accept"),
        /**
         * A response indicating that the client does not wish to accept the
         * proposed challenge and that the server <strong>must not</strong>
         * initiate a game.
         */
        REJECT("reject");
        
        /**
         * Gets a unique string that should be used when sending a packet to
         * indicate this response to a challenge.
         */
        public final String identifier;
        
        private Response(String identifier) {
            this.identifier = identifier;
        }
        
        /**
         * Gets a response based on the identifier found in a packet. If the
         * given identifier was not found, {@link Response#REJECT} is assumed.
         * 
         * @param identifier The identifier of the response to be found.
         * @return The response based on the identifier, or
         *         {@link Response#REJECT} if no response exists with the given
         *         identifier.
         */
        public static Response getByIdentifier(String identifier) {
            for (Response r : Response.values()) {
                if (r.identifier.equalsIgnoreCase(identifier)) {
                    return r;
                }
            }
            
            return Response.REJECT;
        }
    }
}
