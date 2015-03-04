package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet sent by a client to request that a new user account be created and
 * then used to authenticate. This packet is only valid from client to server,
 * and can only be sent during the authentication phase of the handshake
 * process. This packet should <strong>only be sent</strong> if the server
 * returns a {@link PacketServerHandshake} during the initial handshake for
 * which {@link PacketServerHandshake#isRegistrationAllowed()} returns
 * {@code true}. Attempts to register a new user account on a server which does
 * not allow this will result in immediate disconnection.
 * <p>
 * For security, this packet should <strong>only be sent</strong> after
 * encryption has been initialized using a {@link PacketStartEncrypt}.
 * <p>
 * When received by a server, the server should, at its discretion, attempt to
 * create the requested user account and return a {@link PacketAuthResult}
 * containing the result of the registration attempt. It is acceptable for the
 * server to reject a registration attempt <strong>for any reason</strong>,
 * including those for which an explicit result is not defined.
 *
 * @author Ben Thomas
 */
public class PacketRegister extends Packet {
    /**
     * The unique packet identifier used to represent a registration packet.
     */
    public static final int PACKET_ID = 5;
    
    private String username;
    private String password;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketRegister() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param username The username that the client wishes to register.
     * @param password The password that the client is requesting that the
     *            account be created with.
     */
    public PacketRegister(String username, String password) {
        super(PACKET_ID);
        
        this.username = username;
        this.password = password;
    }
    
    /**
     * Gets the username that the client wishes to register as. It is acceptable
     * for the server to register a user account with a different name than
     * provided by the client, for instance if the server wishes to change the
     * capitalisation of the username.
     * 
     * @return The username that the client wishes to register.
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Gets the password that the client wishes for the new account to be
     * registered with. The server is free to place any arbitrary restrictions
     * on which passwords it will accept, however it <strong>must</strong>
     * either register the account with the given password or return a failure.
     * 
     * @return The password that the client wishes to be given to the new
     *         account.
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
