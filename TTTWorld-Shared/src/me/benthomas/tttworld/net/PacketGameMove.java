package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

public class PacketGameMove extends Packet {
    public static final int PACKET_ID = 14;
    
    private UUID gameId;
    private int x, y;
    
    public PacketGameMove() {
        super(PACKET_ID);
    }
    
    public PacketGameMove(UUID gameId, int x, int y) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.x = x;
        this.y = y;
    }
    
    public UUID getGameId() {
        return this.gameId;
    }
    
    public int getX() {
        return this.x;
    }
    
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
