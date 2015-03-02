package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketAuthResult extends Packet {
    public static final int PACKET_ID = 6;
    
    private Result result;
    private String username;
    private boolean admin;
    
    public PacketAuthResult() {
        super(PACKET_ID);
    }
    
    public PacketAuthResult(Result result, String username, boolean admin) {
        super(PACKET_ID);
        
        this.result = result;
        this.username = username;
        this.admin = admin;
    }
    
    public Result getResult() {
        return this.result;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public boolean isAdmin() {
        return this.admin;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("result", this.result.identifier);
        
        if (this.result == Result.OK) {
            o.put("username", this.username);
            o.put("admin", this.admin);
        }
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.result = Result.getByIdentifier(o.getString("result"));
        this.username = (o.has("username")) ? o.getString("username") : null;
        this.admin = (o.has("admin")) ? o.getBoolean("admin") : false;
    }
    
    public enum Result {
        OK("ok"), BAD_CRED("bad_cred"), BANNED("ban"), NAME_TAKEN("name_taken"), PASSWORD_SHORT("password_short"), UNKNOWN(null);
        
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
