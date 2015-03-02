package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthenticate;
import me.benthomas.tttworld.net.PacketChallengeResponse;
import me.benthomas.tttworld.net.PacketGameMove;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.PacketPasswordChange;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.TTTWConnection.DisconnectListener;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Account;

public class AuthenticateHandler implements PacketHandler<PacketAuthenticate> {
    private TTTWClientConnection client;
    
    public static void onAuthenticated(TTTWClientConnection client) throws IOException {
        client.setHandler(PacketAuthenticate.class, null);
        client.setHandler(PacketRegister.class, null);
        
        client.setHandler(PacketPasswordChange.class, new PasswordChangeHandler(client));
        client.setHandler(PacketGlobalChat.class, new GlobalChatHandler(client));
        
        client.setHandler(PacketChallengeResponse.class, new ChallengeResponseHandler(client));
        
        client.setHandler(PacketGameMove.class, new GameMoveHandler(client));
        
        client.sendPacket(new PacketGlobalChat("Welcome to Tic-Tac-Toe World!"));
        client.getServer().sendGlobalBroadcast(client.getAccount().getName() + " has connected!");
        client.getServer().sendPlayerList();
    }
    
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
