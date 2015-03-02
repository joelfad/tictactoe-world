package me.benthomas.tttworld.net;

import org.json.JSONObject;

public class PacketClientHandshake extends Packet {
    public static final int PACKET_ID = 1;
    
    private int protocolVersionMajor;
    private int protocolVersionMinor;
    
    public PacketClientHandshake() {
        super(PACKET_ID);
    }
    
    public PacketClientHandshake(int protocolVersionMajor, int protocolVersionMinor) {
        super(PACKET_ID);
        
        this.protocolVersionMajor = protocolVersionMajor;
        this.protocolVersionMinor = protocolVersionMinor;
    }
    
    public int getMajorProtocolVersion() {
        return this.protocolVersionMajor;
    }
    
    public int getMinorProtocolVersion() {
        return this.protocolVersionMinor;
    }

    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        JSONObject v = new JSONObject();
        
        v.put("major", this.protocolVersionMajor);
        v.put("minor", this.protocolVersionMinor);
        
        o.put("protocol_version", v);
        
        return o;
    }

    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.protocolVersionMajor = o.getJSONObject("protocol_version").getInt("major");
        this.protocolVersionMinor = o.getJSONObject("protocol_version").getInt("minor");
    }
}
