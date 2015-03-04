package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet sent by a client to request a password change or reset. If the
 * username provided by the client was {@code null}, this should be treated as a
 * password change on their current account. Otherwise, this should be treated
 * as an attempt to reset the password of the provided user account. This packet
 * is only valid from client to server. Packets with a non-{@code null} username
 * should <strong>only</strong> be sent by clients who are authenticated as
 * administrators. This packet <strong>must</strong> only be sent after the
 * handshake is completed.
 * <p>
 * When received by a server, the server should attempt to change/reset the
 * given password and send back a {@link PacketPasswordChangeResult} with the
 * result. The server is free to reject a password change/reset for <strong>any
 * reason</strong>.
 *
 * @author Ben Thomas
 */
public class PacketPasswordChange extends Packet {
    /**
     * The unique packet identifier used to represent a password change packet.
     */
    public static final int PACKET_ID = 7;
    
    private String username;
    private String password;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketPasswordChange() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param username The username of the user to reset the password for, or
     *            {@code null} to change the password of the authenticated user.
     * @param password The password to which the reuqested user account's
     *            password should be changed to.
     */
    public PacketPasswordChange(String username, String password) {
        super(PACKET_ID);
        
        this.username = username;
        this.password = password;
    }
    
    /**
     * Gets the username of the user for which the password should be reset. If
     * {@code null}, the password of the current user should be changed.
     * 
     * @return The username for which the password should be reset or
     *         {@code null} for the current user.
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Gets the password to which the given user account should have its
     * password changed. The server is free to reject any given password for any
     * reason, but it <strong>must</strong> either change the password to the
     * desired password or return a failure.
     * 
     * @return The password to which the user's password should be changed.
     */
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        if (this.username != null) {
            o.put("username", this.username);
        }
        
        o.put("password", this.password);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.username = (o.has("username")) ? o.getString("username") : null;
        this.password = o.getString("password");
    }
}
