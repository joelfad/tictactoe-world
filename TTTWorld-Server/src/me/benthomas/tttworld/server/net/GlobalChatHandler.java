package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.command.CommandExecutor;

/**
 * A class which is capable of handling any global chat messages received by a
 * client through a {@link PacketGlobalChat}. If the chat message begins with a
 * colon, it is interpreted as a command and executed; otherwise, it is sent as
 * a global chat message to all other connected users.
 *
 * @author Ben Thomas
 */
public class GlobalChatHandler implements PacketHandler<PacketGlobalChat> {
    private TTTWClientConnection client;
    
    /**
     * Creates a new global chat handler for the given client.
     * 
     * @param client The client for which this handler should handle chat
     *            messages.
     */
    public GlobalChatHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketGlobalChat packet) throws IOException {
        if (packet.getMessage().startsWith(":")) {
            CommandExecutor.executeCommand(this.client, packet.getMessage().split(" "));
        } else {
            this.client.getServer().sendGlobalBroadcast("<" + this.client.getAccount().getName() + "> " + packet.getMessage());
        }
    }
    
}
