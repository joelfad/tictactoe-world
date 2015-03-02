package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketDisconnect extends Packet {
    public static final int PACKET_ID = 0;
    
    private String reason;
    
    public PacketDisconnect() {
        super(PACKET_ID);
    }
    
    public PacketDisconnect(String reason) {
        super(PACKET_ID);
        
        this.reason = reason;
    }
    
    public String getReason() {
        return this.reason;
    }

    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("disconnect_reason", this.reason);
        
        return o;
    }

    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.reason = o.getString("disconnect_reason");
    }
}
