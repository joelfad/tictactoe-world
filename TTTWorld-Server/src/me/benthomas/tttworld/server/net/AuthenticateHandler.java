package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthenticate;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.PacketPasswordChange;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.TTTWConnection.DisconnectListener;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Account;

/**
 * A class which is capable of handling {@link PacketAuthenticate}s from a
 * client and attempts to authenticate them. When the attempt is complete, a
 * {@link PacketAuthResult} is sent back to the client.
 * <p>
 * If the attempt is successful, normal operation is enabled for the client and
 * all regular handlers are automatically registered on the client.
 *
 * @author Ben Thomas
 */
public class AuthenticateHandler implements PacketHandler<PacketAuthenticate> {
    private TTTWClientConnection client;
    
    /**
     * Causes the specified client to enter normal operation after having
     * authenticated successfully. All other connected users are also notified
     * about the new user.
     * 
     * @param client The client which has been successfully authenticated.
     * @throws IOException An unrecoverable error occurred while initialising
     *             the user. Throwing this should cause the user to be
     *             disconnected.
     */
    public static void onAuthenticated(TTTWClientConnection client) throws IOException {
        client.setDefaultHandler(PacketAuthenticate.class, null);
        client.setDefaultHandler(PacketRegister.class, null);
        
        client.setDefaultHandler(PacketPasswordChange.class, new PasswordChangeHandler(client));
        client.setDefaultHandler(PacketGlobalChat.class, new GlobalChatHandler(client));
        
        client.sendMessage("Welcome to Tic-Tac-Toe World!");
        client.getServer().sendGlobalBroadcast(client.getAccount().getName() + " has connected!");
        client.getServer().sendPlayerList();
    }
    
    /**
     * Constructs a new authentication handler which can handle authentication
     * for the given client.
     * 
     * @param client The client for which this handler should handle
     *            authentication attempts.
     */
    public AuthenticateHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketAuthenticate packet) throws IOException {
        Account a = this.client.getServer().getAccountManager().getAccount(packet.getUsername());
        
        if (a == null || !a.isPassword(packet.getPassword())) {
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.BAD_CRED, null, false));
        } else if (a.isBanned()) {
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.BANNED, null, false));
        } else {
            synchronized (this.client.getServer().getConnectedPlayers()) {
                for (TTTWClientConnection otherClient : this.client.getServer().getConnectedPlayers()) {
                    if (otherClient.getAccount() == a) {
                        otherClient.disconnect("Logged in elsewhere!");
                    }
                }
            }
            
            this.client.setAccount(a);
            this.client.getServer().getAccountManager().setAccountSticky(a.getName(), true);
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.OK, a.getName(), a.isAdmin()));
            
            this.client.addDisconnectListener(new DisconnectListener() {
                @Override
                public void onDisconnect(boolean fromRemote, String reason) {
                    AuthenticateHandler.this.client.getServer().getAccountManager()
                            .setAccountSticky(AuthenticateHandler.this.client.getAccount().getName(), false);
                }
            });
            
            AuthenticateHandler.onAuthenticated(this.client);
            
            synchronized (System.out) {
                System.out.println(this.client.getAddress() + " has been authenticated");
            }
        }
    }
    
}
