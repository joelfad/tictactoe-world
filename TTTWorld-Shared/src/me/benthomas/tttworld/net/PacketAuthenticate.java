package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet sent by a client to authenticate to the server using an existing
 * user account. This packet is only valid when sent from client to server, and
 * can only be sent during the authentication phase of the handshake process.
 * <p>
 * For security, this packet should <strong>only be sent</strong> after
 * encryption has been initialized using a {@link PacketStartEncrypt}.
 * <p>
 * When received by a server, the server should attempt to authenticate the user
 * using the provided credentials. After doing so, the server should sent back a
 * {@link PacketAuthResult} containing the result of the authentication attempt.
 * It is acceptable for the server to reject an authentication attempt
 * <strong>for any reason</strong>, including those for which an explicit result
 * is not defined.
 *
 * @author Ben Thomas
 */
public class PacketAuthenticate extends Packet {
    /**
     * The unique packet identifier used to represent an authentication packet.
     */
    public static final int PACKET_ID = 4;
    
    private String username;
    private String password;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketAuthenticate() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param username The username that the client wishes to authenticate as.
     * @param password The password that the client is using the authenticate
     *            itself.
     */
    public PacketAuthenticate(String username, String password) {
        super(PACKET_ID);
        
        this.username = username;
        this.password = password;
    }
    
    /**
     * Gets the username that the client wishes to authenticate as. It is
     * acceptable for a server to authenticate the client with a different
     * username if another name for the user exists.
     * 
     * @return The username that the client is authenticating as.
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Gets the password with which the client is authenticating.
     * 
     * @return The password that the client is authenticating with.
     */
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
