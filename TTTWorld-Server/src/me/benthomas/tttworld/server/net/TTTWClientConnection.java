package me.benthomas.tttworld.server.net;

import java.io.IOException;
import java.net.Socket;

import me.benthomas.tttworld.net.PacketClientHandshake;
import me.benthomas.tttworld.net.PacketStartEncrypt;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.server.Account;
import me.benthomas.tttworld.server.Server;

public class TTTWClientConnection extends TTTWConnection {
    private boolean handling = false;
    private boolean disconnecting = false;
    
    private Server server;
    
    private Account account = null;
    
    public TTTWClientConnection(Socket socket, Server server) throws IOException {
        super(socket);
        
        this.server = server;
        
        this.setHandler(PacketClientHandshake.class, new HandshakeHandler(this));
        this.setHandler(PacketStartEncrypt.class, new StartEncryptHandler(this));
        
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

    public boolean isHandling() {
        return this.handling;
    }
    
    public void setHandling(boolean handling) {
        this.handling = handling;
    }
    
    public boolean isDisconnecting() {
        return this.disconnecting;
    }
    
    public Server getServer() {
        return this.server;
    }
    
    public Account getAccount() {
        return this.account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public class DisconnectLogger implements DisconnectListener {
        
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
                TTTWClientConnection.this.server.sendGlobalBroadcast(TTTWClientConnection.this.account.getName() + " has disconnected!");
                TTTWClientConnection.this.server.sendPlayerList();
            }
        }
        
    }
}
