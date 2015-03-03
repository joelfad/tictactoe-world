package me.benthomas.tttworld.net;

import java.util.UUID;

import org.json.JSONObject;

public class PacketChallengeCancel extends Packet {
    public static final int PACKET_ID = 16;
    
    private UUID challengeId;
    
    public PacketChallengeCancel() {
        super(PACKET_ID);
    }
    
    public PacketChallengeCancel(UUID challengeId) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
    }
    
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.challengeId.toString());
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.challengeId = UUID.fromString(o.getString("id"));
    }
}
