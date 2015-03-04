package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

/**
 * A packet indicating that a given game has ended. This packet is only valid
 * from server to client, on a game previously referenced by a
 * {@link PacketGameUpdate}. After sending this packet, the server <strong>must
 * not</strong> send any further {@link PacketGameUpdate}s referencing this
 * game.
 * <p>
 * When received by a client, the client should mark the referenced game as
 * having ended. After receiving this packet, the client <strong>must
 * not</strong> send any further {@link PacketGameMove}s referencing this game.
 *
 * @author Ben Thomas
 */
public class PacketGameOver extends Packet {
    /**
     * The unique packet identifier used to represent a game over packet.
     */
    public static final int PACKET_ID = 15;
    
    private UUID gameId;
    private Result result;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketGameOver() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param gameId The unique ID of the game which has ended.
     * @param result The result of the game.
     */
    public PacketGameOver(UUID gameId, Result result) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.result = result;
    }
    
    /**
     * Gets the unique ID of the game which has ended. The client and server
     * <strong>must not</strong> send any further {@link PacketGameUpdate}s or
     * {@link PacketGameMove}s referencing this game.
     * 
     * @return The unique ID of the game which has ended.
     */
    public UUID getGameId() {
        return this.gameId;
    }
    
    /**
     * Gets the final result of the game. This should be displayed to the end
     * user to notify them of the outcome of the game.
     * 
     * @return The final result of the game.
     */
    public Result getResult() {
        return this.result;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.gameId.toString());
        o.put("result", this.result.identifier);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.gameId = UUID.fromString(o.getString("id"));
        this.result = Result.getByIdentifier(o.getString("result"));
    }
    
    /**
     * A value indicating the outcome of a game. This should be shown to the
     * user when notifying them that the game has ended.
     *
     * @author Ben Thomas
     */
    public enum Result {
        /**
         * A value indicating that this client has won the referenced game.
         */
        WON("won"),
        /**
         * A value indicating that this client has lost the referenced game.
         */
        LOST("lost"),
        /**
         * A value indicating that the referenced game ended in a draw.
         */
        DRAWN("drawn"),
        /**
         * A value indicating an unknown outcome to the game.
         */
        UNKNOWN(null);
        
        /**
         * A unique string which should be used when sending a packet to
         * indicate this outcome.
         */
        public final String identifier;
        
        private Result(String identifier) {
            this.identifier = identifier;
        }
        
        /**
         * Gets a result based on the identifier found in a packet. If the given
         * identifier was not found, {@link Result#UNKNOWN} is returned.
         * 
         * @param identifier The identifier of the result to be found.
         * @return The result based on the identifier given, or
         *         {@link Result#UNKNOWN} if no result with the given identifier
         *         exists.
         */
        public static Result getByIdentifier(String identifier) {
            for (Result r : Result.values()) {
                if (r.identifier.equalsIgnoreCase(identifier)) {
                    return r;
                }
            }
            
            return Result.UNKNOWN;
        }
    }
    
}
