package me.benthomas.tttworld.net;

import org.json.JSONObject;

/**
 * A packet used to send a global chat message. This packet can be sent in
 * either direction, but its meaning is slightly different based on its
 * direction. A packet of this type may be sent at any time after the handshake
 * is completed.
 * <p>
 * When received by a client, this packet is used to tell the client to display
 * a message in its chat window. The client should display this message to the
 * user.
 * <p>
 * When received by a server, this packet is used to tell the server that the
 * client wishes to send a chat message. If the message begins with ":", this
 * should be interpreted as a command and executed. Otherwise, the message
 * should be sent out over global chat to all other connected players.
 *
 * @author Ben Thomas
 */
public class PacketGlobalChat extends Packet {
    /**
     * The unique packet identifier used to represent a global chat packet.
     */
    public static final int PACKET_ID = 9;
    
    private String message;
    
    /**
     * Creates a new packet into which values can be read from an encoded
     * format.
     */
    public PacketGlobalChat() {
        super(PACKET_ID);
    }
    
    /**
     * Creates a new packet to be sent with the requested values.
     * 
     * @param message If bound for a client, the message that the client should
     *            display. If bound for a server, the message the user wishes to
     *            send.
     */
    public PacketGlobalChat(String message) {
        super(PACKET_ID);
        
        this.message = message;;
    }
    
    /**
     * Gets the message associated with this packet. When bound for a client,
     * this message is the text that should be displayed in the chat window.
     * When bound for a server, it is either a message to be sent to all other
     * users or a command to be executed.
     * 
     * @return The message associated with this packet.
     */
    public String getMessage() {
        return this.message;
    }
    
    @Override
    public JSONObject write() {
        JSONObject o = super.write();
        
        o.put("chat_message", this.message);
        
        return o;
    }
    
    @Override
    public void read(JSONObject o) {
        super.read(o);
        
        this.message = o.getString("chat_message");
    }
}
