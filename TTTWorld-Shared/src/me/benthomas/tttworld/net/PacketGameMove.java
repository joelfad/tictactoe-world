package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

/**
 * A packet sent to make a move for a game. This packet is only valid from
 * client to server, and <strong>must</strong> reference an existing game. A
 * move with coordinates of {@code (-1, -1)} indicates that the user is
 * forfeiting the game, which can be done to any game this client is playing at
 * any time. Any other coordinates indicate that a move should be made by this
 * client at the requested coordinates, and <strong>must</strong> only be sent
 * if the last {@link PacketGameUpdate} referencing this game had
 * {@link PacketGameUpdate#isYourTurn()} evaluating to {@code true}.
 * <p>
 * When received by a server with coordinates of {@code (-1, -1)}, the server
 * should halt the referenced game in the opposing player's favour. If the
 * referenced game does not exist, or the client is not part of that game, the
 * client <strong>should</strong> be disconnected.
 * <p>
 * When received by a server with coordinates other than {@code (-1, -1)}, the
 * server should attempt to make the move requested by the client. If the
 * referenced game does not exist or the client is not playing in the referenced
 * game, the client <strong>should</strong> be disconnected. If the move cannot
 * be made, either because the space is already filled, the move is out of
 * bounds, or because the it is not this client's turn, the client
 * <strong>may</strong> be disconnected or the packet simply ignored.
 *
 * @author Ben Thomas
 */
public class PacketGameMove extends Packet {
    /**
     * The unique packet identifier used to represent a game move packet.
     */
    public static final int PACKET_ID = 14;
    
    private UUID gameId;
    private int x, y;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketGameMove() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param gameId The unique ID of the game to which this move should be
     *            made.
     * @param x The x coordinate at which the move should be made.
     * @param y The y coordinate at which the move should be made.
     */
    public PacketGameMove(UUID gameId, int x, int y) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Gets the unique ID of the game to which the move should be made.
     * 
     * @return The unique ID of the game to make this move on.
     */
    public UUID getGameId() {
        return this.gameId;
    }
    
    /**
     * Gets the x coordinate at which the move should be made. If the
     * coordinates of the move are {@code (-1, -1)}, this indicates that the
     * player is forfeiting the game.
     * 
     * @return The x coordinate at which the move should be made.
     */
    public int getX() {
        return this.x;
    }
    
    /**
     * Gets the y coordinate at which the move should be made. If the coordinate
     * of the move are {@code (-1, -1)}, this indicates that the player is
     * forfeiting the game.
     * 
     * @return The y coordinate at which the move should be made.
     */
    public int getY() {
        return this.y;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.gameId.toString());
        o.put("x", this.x);
        o.put("y", this.y);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.gameId = UUID.fromString(o.getString("id"));
        this.x = o.getInt("x");
        this.y = o.getInt("y");
    }
    
}
