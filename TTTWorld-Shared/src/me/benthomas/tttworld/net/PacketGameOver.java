package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

public class PacketGameOver extends Packet {
    public static final int PACKET_ID = 15;
    
    private UUID gameId;
    private Result result;
    
    public PacketGameOver() {
        super(PACKET_ID);
    }
    
    public PacketGameOver(UUID gameId, Result result) {
        super(PACKET_ID);
        
        this.gameId = gameId;
        this.result = result;
    }
    
    public UUID getGameId() {
        return this.gameId;
    }
    
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
    
    public enum Result {
        WON("won"), LOST("lost"), DRAWN("drawn"), UNKNOWN(null);
        
        public final String identifier;
        
        private Result(String identifier) {
            this.identifier = identifier;
        }
        
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
