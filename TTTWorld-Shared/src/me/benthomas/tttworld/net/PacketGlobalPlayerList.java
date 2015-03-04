package me.benthomas.tttworld.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A packet sent by the server to signal that the list of connected users has
 * been updated. This packet is only valid from server to client, and can be
 * sent at any time after the handshake is complete. This packet is generally
 * sent to all connected users when a new user has connected or when a user has
 * disconnected and the user list must be updated.
 * <p>
 * When received by a client, the client should update its list of players
 * accordingly. No further action by the client is required.
 *
 * @author Ben Thomas
 */
public class PacketGlobalPlayerList extends Packet {
    /**
     * The unique packet identifier used to represent a global player list
     * update packet.
     */
    public static final int PACKET_ID = 10;
    
    private List<PlayerInfo> players;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketGlobalPlayerList() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param playerList The list of connected users that the client should
     *            display. <strong>Must not</strong> include the user
     *            themselves.
     */
    public PacketGlobalPlayerList(List<PlayerInfo> playerList) {
        super(PACKET_ID);
        
        this.players = playerList;
    }
    
    /**
     * Gets the list of currently connected users that should be displayed to
     * the end user. <strong>Must not</strong> contain the user associated with
     * this client.
     * 
     * @return The updated list of connected players.
     */
    public List<PlayerInfo> getPlayers() {
        return this.players;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        JSONArray players = new JSONArray();
        
        for (PlayerInfo p : this.players) {
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
    
    /**
     * Provides basic information regarding a connected user that can be
     * displayed to the end user.
     *
     * @author Ben Thomas
     */
    public static class PlayerInfo implements Comparable<PlayerInfo> {
        /**
         * The username of the user represented by this instance.
         */
        public final String username;
        
        /**
         * Whether or not the user represented by this instance is an
         * administrator. This is optional information provided by the server,
         * and <strong>should not</strong> be relied upon.
         */
        public final boolean admin;
        
        /**
         * Creates a new instance with the given properties.
         * 
         * @param username The username of the connected user represented by
         *            this instance.
         * @param admin Whether or not the user represented by this instance is
         *            an administrator. This is <strong>optional
         *            information</strong>; if not provided, use {@code false}.
         */
        public PlayerInfo(String username, boolean admin) {
            this.username = username;
            this.admin = admin;
        }
        
        /**
         * Parses information from the given JSON representation.
         * 
         * @param o The JSON representation of this instance.
         */
        public PlayerInfo(JSONObject o) {
            this.username = o.getString("username");
            this.admin = o.getBoolean("admin");
        }
        
        /**
         * Creates a JSON representation encapsulating the data represented by
         * this instance.
         * 
         * @return A JSON representation of this instance.
         */
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
