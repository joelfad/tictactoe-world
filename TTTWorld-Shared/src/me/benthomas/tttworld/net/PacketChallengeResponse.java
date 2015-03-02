package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

public class PacketChallengeResponse extends Packet {
    public static final int PACKET_ID = 12;
    
    private UUID challengeId;
    private Response response;
    
    public PacketChallengeResponse() {
        super(PACKET_ID);
    }
    
    public PacketChallengeResponse(UUID challengeId, Response response) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
        this.response = response;
    }
    
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
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
    
    public enum Response {
        ACCEPT("accept"), REJECT("reject"), UNKNOWN(null);
        
        public final String identifier;
        
        private Response(String identifier) {
            this.identifier = identifier;
        }
        
        public static Response getByIdentifier(String identifier) {
            for (Response r : Response.values()) {
                if (r.identifier.equalsIgnoreCase(identifier)) {
                    return r;
                }
            }
            
            return Response.UNKNOWN;
        }
    }
}
