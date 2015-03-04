package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet which is sent to indicate the result of a password change or reset.
 * This packet is only valid from server to client, with exactly one packet
 * being sent immediately following a {@link PacketPasswordChange}.
 * <p>
 * When receiving this packet, a client need not take any action unless it needs
 * to inform the user of the result. A result of {@link Result#OK} is considered
 * a success, while all other results should be considered failure.
 *
 * @author Ben Thomas
 */
public class PacketPasswordChangeResult extends Packet {
    /**
     * The unique packet identifier used to represent a password change result
     * packet.
     */
    public static final int PACKET_ID = 8;
    
    private Result result;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketPasswordChangeResult() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param result The result of the password change.
     */
    public PacketPasswordChangeResult(Result result) {
        super(PACKET_ID);
        
        this.result = result;
    }
    
    /**
     * Gets the result of the attempted password change. A result of
     * {@link Result#OK} indicates success, while all other results indicate
     * failure.
     * 
     * @return The result of the password change operation.
     */
    public Result getResult() {
        return this.result;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("result", this.result.identifier);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.result = Result.getByIdentifier(o.getString("result"));
    }
    
    /**
     * A value indicating the result of an attempted password change or reset.
     * Only a result of {@link Result#OK} indicates success. All other values
     * indicate some sort of failure.
     *
     * @author Ben Thomas
     */
    public enum Result {
        /**
         * A result indicating that the password change or reset was performed
         * successfully.
         */
        OK("ok"),
        /**
         * A result indicating that the provided password was too short or was
         * rejected by the server for some other reason. The client may attempt
         * this action again with a different password.
         */
        PASSWORD_SHORT("password_short"),
        /**
         * A result indicating that the user account the client is authenticated
         * as is not allowed to perform the requested action. The client
         * <strong>should not</strong> retry a request after receiving this
         * error.
         */
        NOT_ALLOWED("not_allowed"),
        /**
         * A result indicating that the user for which the password should be
         * reset was not found. The client may retry with a different username.
         */
        USER_NOT_FOUND("user_not_found"),
        /**
         * Indicates an unknown failure condition while resetting or changing
         * the requested password.
         */
        UNKNOWN(null);
        
        /**
         * Gets a unique string that should be used when sending a packet to
         * indicate this result.
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
