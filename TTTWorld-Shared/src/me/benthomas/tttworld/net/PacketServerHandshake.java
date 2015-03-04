package me.benthomas.tttworld.net;

import me.benthomas.tttworld.Crypto;

import org.json.JSONObject;

/**
 * A packet sent by the server to provide information to the client during the
 * handshake process. This packet is only valid when sent from a server to a
 * client. Only one handshake packet may be sent, immediately after receiving a
 * {@link PacketClientHandshake}.
 * <p>
 * When received by a client, the client should first use the information
 * provided to send a {@link PacketStartEncrypt} to initialize encryption.
 * Following this, the client should send a {@link PacketAuthenticate} or a
 * {@link PacketRegister} (if allowed) to authenticate to the server.
 *
 * @author Ben Thomas
 */
public class PacketServerHandshake extends Packet {
    /**
     * The unique packet identifier used to represent a server handshake packet.
     */
    public final static int PACKET_ID = 2;
    
    private int compressThreshold;
    
    private String serverName;
    private boolean registerAllowed;
    
    private byte[] publicKey;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketServerHandshake() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param compressThreshold The compression threshold that the client should
     *            use when communicating with the server.
     * @param serverName The name of the server to which the client is
     *            connecting.
     * @param registerAllowed Whether the server allows users to self register
     *            for an account.
     * @param publicKey The server's public RSA key.
     */
    public PacketServerHandshake(int compressThreshold, String serverName, boolean registerAllowed, byte[] publicKey) {
        super(PACKET_ID);
        
        this.compressThreshold = compressThreshold;
        this.serverName = serverName;
        this.registerAllowed = registerAllowed;
        this.publicKey = publicKey;
    }
    
    /**
     * Gets the size in bytes of a packet payload that should trigger
     * compression of the packet payload. This is merely a suggestion by the
     * server, and the use of encryption for outgoing packets is <strong>enirely
     * voluntary</strong> on the part of the client.
     * 
     * @return The size of an uncompressed JSON packet at which the packet
     *         should be compressed.
     */
    public int getCompressionThreshold() {
        return this.compressThreshold;
    }
    
    /**
     * Gets the name of the server the client is connecting to. This is simply a
     * convenience to the end user and should not be used to verify the identity
     * of the server.
     * 
     * @return The name of the server to which the client is connecting.
     */
    public String getServerName() {
        return this.serverName;
    }
    
    /**
     * Gets a boolean value indicating whether the server being connected to
     * allows self-registration. If this value is {@code false}, sending a
     * {@link PacketRegister} is not allowable, and will result in an immediate
     * disconnection.
     * 
     * @return Whether the server allows self-registration of user accounts.
     */
    public boolean isRegistrationAllowed() {
        return this.registerAllowed;
    }
    
    /**
     * Gets the RSA public key of the server being connected to. The SHA-1
     * fingerprint of this public key <strong>should be verified</strong> and
     * stored before the client proceeds any further.
     * 
     * @return The RSA public key of the server.
     */
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
            o.put("public_key", Crypto.encodeToBase64(this.publicKey));
        }
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.compressThreshold = o.getInt("compress_threshold");
        this.serverName = o.getString("server_name");
        this.registerAllowed = o.getBoolean("register_allowed");
        this.publicKey = Crypto.decodeFromBase64(o.getString("public_key"));
    }
}
