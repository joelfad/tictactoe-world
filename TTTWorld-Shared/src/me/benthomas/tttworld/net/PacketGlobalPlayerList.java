package me.benthomas.tttworld.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class PacketGlobalPlayerList extends Packet {
    public static final int PACKET_ID = 10;
    
    private List<PlayerInfo> players;
    
    public PacketGlobalPlayerList() {
        super(PACKET_ID);
    }
    
    public PacketGlobalPlayerList(List<PlayerInfo> playerList) {
        super(PACKET_ID);
        
        this.players = playerList;
    }
    
    public List<PlayerInfo> getPlayers() {
        return this.players;
    }

    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        JSONArray players = new JSONArray();
        
        for (PlayerInfo p : this.players){
            players.put(p.write());
        }
        
        o.put("players", players);
        
        return o;
    }

    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.players = new ArrayList<PlayerInfo>();
        
        for (int i = 0; i < o.getJSONArray("players").length(); i++) {
            this.players.add(new PlayerInfo(o.getJSONArray("players").getJSONObject(i)));
        }
    }
    
    public static class PlayerInfo implements Comparable<PlayerInfo> {
        public final String username;
        public final boolean admin;
        
        public PlayerInfo(String username, boolean admin) {
            this.username = username;
            this.admin = admin;
        }
        
        public PlayerInfo(JSONObject o) {
            this.username = o.getString("username");
            this.admin = o.getBoolean("admin");
        }
        
        public JSONObject write() {
            JSONObject o = new JSONObject();
            
            o.put("username", this.username);
            o.put("admin", this.admin);
            
            return o;
        }

        @Override
        public int compareTo(PlayerInfo other) {
            return this.username.compareTo(other.username);
        }

        @Override
        public String toString() {
            if (this.admin) {
                return "* " + this.username;
            } else {
                return this.username;
            }
        }
    }
}
