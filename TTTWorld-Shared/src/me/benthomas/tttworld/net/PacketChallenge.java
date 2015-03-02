package me.benthomas.tttworld.net;

import java.util.UUID;

import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;

import org.json.JSONObject;

public class PacketChallenge extends Packet {
    public static final int PACKET_ID = 11;
    
    private UUID challengeId;
    private PlayerInfo sender;
    private long timeout;
    
    public PacketChallenge() {
        super(PACKET_ID);
    }
    
    public PacketChallenge(UUID challengeId, PlayerInfo sender, long timeout) {
        super(PACKET_ID);
        
        this.challengeId = challengeId;
        this.sender = sender;
        this.timeout = timeout;
    }
    
    public UUID getChallengeId() {
        return this.challengeId;
    }
    
    public PlayerInfo getSender() {
        return this.sender;
    }
    
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("id", this.challengeId.toString());
        o.put("sender", this.sender.write());
        o.put("timeout", this.timeout);
        
        return o;
    }

    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.challengeId = UUID.fromString(o.getString("id"));
        this.sender = new PlayerInfo(o.getJSONObject("sender"));
        this.timeout = o.getLong("timeout");
    }
}
