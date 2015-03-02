package me.benthomas.tttworld.net;

import java.util.Base64;

import org.json.JSONObject;

public class PacketStartEncrypt extends Packet {
    public static final int PACKET_ID = 3;
    
    private byte[] cryptKey;
    
    public PacketStartEncrypt() {
        super(PACKET_ID);
    }
    
    public PacketStartEncrypt(byte[] cryptKey) {
        super(PACKET_ID);
        
        this.cryptKey = cryptKey;
    }
    
    public byte[] getCryptKey() {
        return this.cryptKey;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("crypt_key", Base64.getEncoder().encodeToString(this.cryptKey));
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.cryptKey = Base64.getDecoder().decode(o.getString("crypt_key"));
    }
    
}
