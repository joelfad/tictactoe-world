package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet sent by a server to indicate the result of an authentication or
 * registration attempt. This packet is only valid when sent from server to
 * client. {@link Result#OK} should be treated as successful
 * authentication/registration and all other values should be treated as
 * failure.
 * <p>
 * In general, this packet is sent once immediately following any
 * {@link PacketAuthenticate} or {@link PacketRegister} sent by a client.
 * However, a server <strong>may</strong>, at its discretion, send another
 * packet with {@link Result#OK} to indicate that one of the parameters provided
 * has changed.
 * <p>
 * When received by a client with {@link Result#OK}, the handshake process is
 * completed and the client may begin normal operation. When received by a
 * client with any other result, the attempt has failed and the client may send
 * another {@link PacketAuthenticate} or {@link PacketRegister} to retry.
 * <p>
 * When received by a client outside of the handshake process with
 * {@link Result#OK}, the client should update its user interface to reflect any
 * changes in the provided parameters.
 *
 * @author Ben Thomas
 */
public class PacketAuthResult extends Packet {
    /**
     * The unique packet identifier used to represent an authentication result
     * packet.
     */
    public static final int PACKET_ID = 6;
    
    private Result result;
    private String username;
    private boolean admin;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketAuthResult() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param result The result of the authentication attempt.
     * @param username If the attempt succeeded, the username under which the
     *            client is now authenticated. Otherwise, {@code null}.
     * @param admin If the attempt succeeded, whether the user the client is
     *            authenticated under has administrator permissions. Otherwise,
     *            {@code false}.
     */
    public PacketAuthResult(Result result, String username, boolean admin) {
        super(PACKET_ID);
        
        this.result = result;
        this.username = username;
        this.admin = admin;
    }
    
    /**
     * Gets the result of the authentication attempt. A result of
     * {@link Result#OK} indicates a succeeded attempt, and any other result
     * indicates failure for some reason.
     * 
     * @return The result of the authentication attempt.
     */
    public Result getResult() {
        return this.result;
    }
    
    /**
     * Gets the username that the client is now authenticated under. This may be
     * different than the username that the client originally used. If the
     * attempt was unsuccessful, returns {@code null}.
     * 
     * @return The username the client is authenticated under, or {@code null}
     *         if the attempt failed.
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Gets a value indicating whether the user the client is now authenticated
     * under has administrator permissions. If the attempt was unsuccessful,
     * returns {@code false}.
     * 
     * @return Whether the user the client is authenticated as is an
     *         administrator, or {@code false} if the attempt failed.
     */
    public boolean isAdmin() {
        return this.admin;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("result", this.result.identifier);
        
        if (this.result == Result.OK) {
            o.put("username", this.username);
            o.put("admin", this.admin);
        }
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.result = Result.getByIdentifier(o.getString("result"));
        this.username = (o.has("username")) ? o.getString("username") : null;
        this.admin = (o.has("admin")) ? o.getBoolean("admin") : false;
    }
    
    /**
     * A value indicating the result of an authentication or registration
     * attempt. Only a result of {@code Result#OK} should be treated as a
     * successful authentication, all other values are reserved for failures.
     *
     * @author Ben Thomas
     */
    public enum Result {
        /**
         * A result indicating that an authentication attempt has succeeded and
         * that the client can begin normal operation.
         */
        OK("ok"),
        /**
         * A result indicating that the credentials that the client provided
         * were incorrect. The client may attempt to authenticate again if it
         * wishes.
         */
        BAD_CRED("bad_cred"),
        /**
         * A result indicating that the username and password given were
         * correct, but that the user in question has been banned from the
         * server. In general, a client <strong>should not</strong> retry an
         * authentication attempt after receiving this result.
         */
        BANNED("ban"),
        /**
         * A result indicating that a registration attempt failed because the
         * name requested is already taken. The client may attempt to register
         * again with another username if it wishes.
         */
        NAME_TAKEN("name_taken"),
        /**
         * A result indicating that a registration attempt failed because the
         * password provided for the account is either too short, or was
         * rejected by the server for some other reason. The client may attempt
         * to register again with another password if it wishes.
         */
        PASSWORD_SHORT("password_short"),
        /**
         * Indicates an unknown failure condition that has occurred while
         * authenticating or registering a user account.
         */
        UNKNOWN(null);
        
        /**
         * A unique string that should be used when sending a packet to indicate
         * this result.
         */
        public final String identifier;
        
        private Result(String identifier) {
            this.identifier = identifier;
        }
        
        /**
         * Gets a result based on the identifier found in a packet. If the given
         * identifier was not found, {@link Result#UNKNOWN} is returned.
         * 
         * @param identifier The identifier of the result to be found.
         * @return The result based on the identifier given, or
         *         {@link Result#UNKNOWN} if no result with the given identifier
         *         exists.
         */
        public static Result getByIdentifier(String identifier) {
            for (Result r : Result.values()) {
                if (r.identifier.equalsIgnoreCase(identifier)) {
                    return r;
                }
            }
            
            return Result.UNKNOWN;
        }
    }
}
