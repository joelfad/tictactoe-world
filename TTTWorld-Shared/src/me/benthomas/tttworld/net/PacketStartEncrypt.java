package me.benthomas.tttworld.net;

import me.benthomas.tttworld.Crypto;

import org.json.JSONObject;

/**
 * A packet sent by the client as part of the handshake process to being
 * encrypting packets. This packet is only valid when sent from a client to a
 * server, and may only be sent once. While this packet may be sent at any time
 * during the connection, knowledge of the server's public RSA key is necessary
 * to send this packet, so it should generally not be sent before a
 * {@link PacketServerHandshake} is received. After being sent by a client, all
 * packets between the client and the server should be encrypted using the
 * agreed-upon AES-128 key.
 * <p>
 * When received by a server, the server <strong>must</strong> decrypt the
 * contained AES-128 key and secure all further communications with the sender
 * using this key. The server does not need to send any reply to this packet.
 *
 * @author Ben Thomas
 */
public class PacketStartEncrypt extends Packet {
    /**
     * The unique packet identifier used to represent an encryption start
     * packet.
     */
    public static final int PACKET_ID = 3;
    
    private byte[] cryptKey;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketStartEncrypt() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param cryptKey The RSA-encrypted AES-128 key that should be used to
     *            secure further communication.
     */
    public PacketStartEncrypt(byte[] cryptKey) {
        super(PACKET_ID);
        
        this.cryptKey = cryptKey;
    }
    
    /**
     * Gets the RSA-encrypted AES-128 key that should be used to secure further
     * communication. This key is encrypted using the server's RSA public key to
     * prevent MITM attacks.
     * <p>
     * After processing this packet, this key should be used to both encrypt and
     * decrypt further communication.
     * 
     * @return The RSA-encrypted AES-128 key that should be used to secure
     *         packets.
     */
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
