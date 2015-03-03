package me.benthomas.tttworld.net;

import me.benthomas.tttworld.Crypto;

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
        
        o.put("crypt_key", Crypto.encodeToBase64(this.cryptKey));
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.cryptKey = Crypto.decodeFromBase64(o.getString("crypt_key"));
    }
    
}
