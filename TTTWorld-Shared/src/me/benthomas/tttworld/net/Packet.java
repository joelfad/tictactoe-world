package me.benthomas.tttworld.net;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Packet {
    private static HashMap<Integer, Class<? extends Packet>> packetTypes = new HashMap<Integer, Class<? extends Packet>>();
    
    static {
        packetTypes.put(PacketKeepAlive.PACKET_ID, PacketKeepAlive.class);
        packetTypes.put(PacketDisconnect.PACKET_ID, PacketDisconnect.class);
        
        packetTypes.put(PacketClientHandshake.PACKET_ID, PacketClientHandshake.class);
        packetTypes.put(PacketServerHandshake.PACKET_ID, PacketServerHandshake.class);
        packetTypes.put(PacketStartEncrypt.PACKET_ID, PacketStartEncrypt.class);
        packetTypes.put(PacketAuthenticate.PACKET_ID, PacketAuthenticate.class);
        packetTypes.put(PacketRegister.PACKET_ID, PacketRegister.class);
        packetTypes.put(PacketAuthResult.PACKET_ID, PacketAuthResult.class);
        
        packetTypes.put(PacketPasswordChange.PACKET_ID, PacketPasswordChange.class);
        packetTypes.put(PacketPasswordChangeResult.PACKET_ID, PacketPasswordChangeResult.class);
        
        packetTypes.put(PacketGlobalChat.PACKET_ID, PacketGlobalChat.class);
        packetTypes.put(PacketGlobalPlayerList.PACKET_ID, PacketGlobalPlayerList.class);
        
        packetTypes.put(PacketChallenge.PACKET_ID, PacketChallenge.class);
        packetTypes.put(PacketChallengeResponse.PACKET_ID, PacketChallengeResponse.class);
        packetTypes.put(PacketChallengeCancel.PACKET_ID, PacketChallengeCancel.class);
        
        packetTypes.put(PacketGameUpdate.PACKET_ID, PacketGameUpdate.class);
        packetTypes.put(PacketGameMove.PACKET_ID, PacketGameMove.class);
        packetTypes.put(PacketGameOver.PACKET_ID, PacketGameOver.class);
    }
    
    public static Packet readPacket(String payload) throws IOException {
        try {
            JSONObject json = new JSONObject(payload);
            Class<? extends Packet> packetType = packetTypes.get(json.getInt("packet_id"));
            
            if (packetType == null) {
                throw new IOException("Invalid packet");
            }
            
            Packet p = packetType.newInstance();
            p.read(json);
            
            return p;
        } catch (JSONException | IllegalArgumentException e) {
            throw new IOException("Invalid packet", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public final int packetId;
    private long timestamp;
    
    public Packet(int packetId) {
        this.packetId = packetId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public final long getTimestamp() {
        return this.timestamp;
    }
    
    public JSONObject write() {
        JSONObject o = new JSONObject();
        
        o.put("packet_id", this.packetId);
        o.put("timestamp", this.timestamp);
        
        return o;
    }
    
    public void read(JSONObject o) {
        this.timestamp = o.getLong("timestamp");
    }
}
