package me.benthomas.tttworld.server.net;

import java.io.IOException;
import java.net.Socket;

import me.benthomas.tttworld.net.PacketClientHandshake;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.PacketStartEncrypt;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.Server;

/**
 * Represents a TTTW-compliant connection to a client connected to this server.
 * Has some additional state information on top of a {@link TTTWConnection} that
 * supports client authentication and other various server-only features.
 * <p>
 * Provides recursive disconnection protection, meaning that any attempt to
 * disconnect a user while they're already being disconnected will be ignored.
 *
 * @author Ben Thomas
 */
public class TTTWClientConnection extends TTTWConnection {
    private boolean handling = false;
    private boolean disconnecting = false;
    
    private Server server;
    
    private Account account = null;
    
    /**
     * Creates a new TTTW-compliant connection to a client on the given socket.
     * 
     * @param socket The socket on which the client is connected.
     * @param server The server that this client is connected to.
     * @throws IOException An unrecoverable error occurred while attempting to
     *             open up communications to the client.
     */
    public TTTWClientConnection(Socket socket, Server server) throws IOException {
        super(socket);
        
        this.server = server;
        
        this.setDefaultHandler(PacketClientHandshake.class, new HandshakeHandler(this));
        this.setDefaultHandler(PacketStartEncrypt.class, new StartEncryptHandler(this));
        
        this.addDisconnectListener(new DisconnectLogger());
    }
    
    @Override
    public String getAddress() {
        if (this.account == null) {
            return super.getAddress();
        } else {
            return super.getAddress() + " [" + this.account.getName() + "]";
        }
    }
    
    /**
     * Gets a value indicating whether or not this TTTW client is in the process
     * of having a packet handled on a packet handling thread. When this returns
     * {@code true}, no attempts should be made to look for awaiting packets and
     * queue this client for packet handling.
     * 
     * @return Whether or not a packet is currently being handled for this
     *         client.
     */
    public boolean isHandling() {
        return this.handling;
    }
    
    /**
     * Marks this client as either having a packet handled for them or not
     * currently handling a packet.
     * 
     * @param handling Whether or not a packet is currently being handled on
     *            behalf of this client.
     */
    public void setHandling(boolean handling) {
        this.handling = handling;
    }
    
    /**
     * Gets a value indicating whether this client is in the process of being
     * disconnected. While {@code true}, any calls to
     * {@link #disconnect(String)} will be ignored to prevent recursive
     * disconnection.
     * 
     * @return Whether this client is in the process of being disconnected from
     *         the server.
     */
    public boolean isDisconnecting() {
        return this.disconnecting;
    }
    
    /**
     * Gets the server that this client is connected to.
     * 
     * @return The server that this client is connected to.
     */
    public Server getServer() {
        return this.server;
    }
    
    /**
     * Gets the account that this client is authenticated as. If this client has
     * not yet authenticated, returns {@code null}.
     * 
     * @return The user account this client is authenticated as or {@code null}
     *         if unauthenticated.
     */
    public Account getAccount() {
        return this.account;
    }
    
    /**
     * Authenticates this client as the given account.
     * 
     * @param account The account that this client is now authenticated as.
     */
    public void setAccount(Account account) {
        this.account = account;
    }
    
    /**
     * Sends a global chat message to this client. If an error occurs while
     * sending the message, the client will be automatically disconnected.
     * 
     * @param message The message that should be sent to this client.
     */
    public void sendMessage(String message) {
        try {
            this.sendPacket(new PacketGlobalChat(message));
        } catch (IOException e) {
            this.disconnect("Error sending packet!");
        }
    }
    
    @Override
    public void disconnect(String message) {
        if (!this.disconnecting) {
            super.disconnect(message);
        }
    }
    
    private class DisconnectLogger implements DisconnectListener {
        
        @Override
        public void onDisconnect(boolean fromRemote, String reason) {
            synchronized (System.out) {
                if (fromRemote) {
                    System.out.println(TTTWClientConnection.this.getAddress() + " has disconnected: " + reason);
                } else {
                    System.out.println(TTTWClientConnection.this.getAddress() + " has been disconnected: " + reason);
                }
            }
            
            TTTWClientConnection.this.disconnecting = true;
            
            if (TTTWClientConnection.this.account != null) {
                TTTWClientConnection.this.server.sendGlobalBroadcast(TTTWClientConnection.this.account.getName()
                        + " has disconnected!");
                TTTWClientConnection.this.server.sendPlayerList();
            }
        }
        
    }
}
