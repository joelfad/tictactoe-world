package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketPasswordChangeResult extends Packet {
    public static final int PACKET_ID = 8;
    
    private Result result;
    
    public PacketPasswordChangeResult() {
        super(PACKET_ID);
    }
    
    public PacketPasswordChangeResult(Result result) {
        super(PACKET_ID);
        
        this.result = result;
    }
    
    public Result getResult() {
        return this.result;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("result", this.result.identifier);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.result = Result.getByIdentifier(o.getString("result"));
    }
    
    public enum Result {
        OK("ok"), PASSWORD_SHORT("password_short"), NOT_ALLOWED("not_allowed"), USER_NOT_FOUND("user_not_found"), UNKNOWN(null);
        
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
