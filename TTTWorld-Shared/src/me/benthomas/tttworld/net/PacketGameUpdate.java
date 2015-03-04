package me.benthomas.tttworld.net;

import java.util.UUID;

import me.benthomas.tttworld.Board;

import org.json.JSONObject;

/**
 * A packet sent to either spawn a new game window or update the board of an
 * existing game window. This packet is only valid from server to client, at any
 * time after the handshake is complete. A server <strong>should not</strong>
 * start new games without action from a client.
 * <p>
 * When received by a client with a game ID which is not currently active, the
 * client should display a new game containing the given board.
 * <p>
 * When received by a client with an active game ID, the board being displayed
 * for that game should be updated to reflect the changes.
 *
 * @author Ben Thomas
 */
public class PacketGameUpdate extends Packet {
    /**
     * The unique packet identifier used to represent a game update packet.
     */
    public static final int PACKET_ID = 13;
    
    private UUID gameId;
    private Board board;
    private boolean yourTurn;
    private boolean gameOver;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketGameUpdate() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param gameId The unique ID of the game which should be updated or
     *            started.
     * @param board The board which should be displayed to the end user.
     * @param yourTurn Whether the server is requesting that this client make a
     *            move for this game.
     * @param gameOver Whether the game has ended.
     */
    public PacketGameUpdate(UUID gameId, Board board, boolean yourTurn, boolean gameOver) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.board = board;
        this.yourTurn = yourTurn;
        this.gameOver = gameOver;
    }
    
    /**
     * Gets the unique ID of the game which should be updated or started. If
     * this ID is already active, the board being displayed for that game should
     * be updated. Otherwise, a new game should be displayed.
     * 
     * @return The unique ID of the game to be updated or started.
     */
    public UUID getGameId() {
        return this.gameId;
    }
    
    /**
     * Gets the board which should be displayed for this game.
     * 
     * @return The board which should be displayed for this game.
     */
    public Board getBoard() {
        return this.board;
    }
    
    /**
     * Gets a value indicating whether the server has requested that this client
     * send a {@link PacketGameMove}. If this value is {@code false}, the client
     * <strong>must not</strong> send any {@link PacketGameMove}s until such a
     * time as this value is {@code true}.
     * 
     * @return Whether the server is requesting that this client make a move.
     */
    public boolean isYourTurn() {
        return this.yourTurn;
    }
    
    /**
     * Gets a value indicating whether this update is the final state of the
     * game. If this is {@code true}, a {@link PacketGameOver}
     * <strong>must</strong> follow this update.
     * 
     * @return Whether the game represented by this packet is over.
     */
    public boolean isGameOver() {
        return this.gameOver;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.gameId.toString());
        o.put("board", this.board.toJsonString());
        o.put("your_turn", this.yourTurn);
        o.put("game_over", this.gameOver);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.gameId = UUID.fromString(o.getString("id"));
        this.board = new Board(o.getString("board"));
        this.yourTurn = o.getBoolean("your_turn");
        this.gameOver = o.getBoolean("game_over");
    }
    
}
