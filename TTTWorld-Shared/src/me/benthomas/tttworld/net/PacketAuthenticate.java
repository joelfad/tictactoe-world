package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketAuthenticate extends Packet {
    public static final int PACKET_ID = 4;
    
    private String username;
    private String password;
    
    public PacketAuthenticate() {
        super(PACKET_ID);
    }
    
    public PacketAuthenticate(String username, String password) {
        super(PACKET_ID);
        
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("username", this.username);
        o.put("password", this.password);
        
        return o;
    }

    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.username = o.getString("username");
        this.password = o.getString("password");
    }
}
