package me.benthomas.tttworld.net;

import java.util.UUID;

import me.benthomas.tttworld.Board;

import org.json.JSONObject;

public class PacketGameUpdate extends Packet {
    public static final int PACKET_ID = 13;
    
    private UUID gameId;
    private Board board;
    private boolean yourTurn;
    private boolean gameOver;
    
    public PacketGameUpdate() {
        super(PACKET_ID);
    }
    
    public PacketGameUpdate(UUID gameId, Board board, boolean yourTurn, boolean gameOver) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.board = board;
        this.yourTurn = yourTurn;
        this.gameOver = gameOver;
    }
    
    public UUID getGameId() {
        return this.gameId;
    }
    
    public Board getBoard() {
        return this.board;
    }
    
    public boolean isYourTurn() {
        return this.yourTurn;
    }
    
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
