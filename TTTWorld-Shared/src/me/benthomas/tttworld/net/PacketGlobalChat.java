package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketGlobalChat extends Packet {
    public static final int PACKET_ID = 9;
    
    private String message;
    
    public PacketGlobalChat() {
        super(PACKET_ID);
    }
    
    public PacketGlobalChat(String message) {
        super(PACKET_ID);
        
        this.message = message;;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("chat_message", this.message);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.message = o.getString("chat_message");
    }
}
