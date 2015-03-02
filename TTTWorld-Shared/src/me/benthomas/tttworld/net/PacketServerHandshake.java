package me.benthomas.tttworld.net;

import java.util.Base64;

import org.json.JSONObject;

public class PacketServerHandshake extends Packet {
    public final static int PACKET_ID = 2;
    
    private int compressThreshold;
    
    private String serverName;
    private boolean registerAllowed;
    
    private byte[] publicKey;
    
    public PacketServerHandshake() {
        super(PACKET_ID);
    }
    
    public PacketServerHandshake(int compressThreshold, String serverName, boolean registerAllowed, byte[] publicKey) {
        super(PACKET_ID);
        
        this.compressThreshold = compressThreshold;
        this.serverName = serverName;
        this.registerAllowed = registerAllowed;
        this.publicKey = publicKey;
    }
    
    public String getServerName() {
        return this.serverName;
    }
    
    public boolean isRegistrationAllowed() {
        return this.registerAllowed;
    }
    
    public byte[] getPublicKey() {
        return this.publicKey;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("compress_threshold", this.compressThreshold);
        o.put("server_name", this.serverName);
        o.put("register_allowed", this.registerAllowed);
        
        if (this.publicKey == null) {
            o.put("public_key", "");
        } else {
            o.put("public_key", Base64.getEncoder().encodeToString(this.publicKey));
        }
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.compressThreshold = o.getInt("compress_threshold");
        this.serverName = o.getString("server_name");
        this.registerAllowed = o.getBoolean("register_allowed");
        this.publicKey = Base64.getDecoder().decode(o.getString("public_key"));
    }
}
