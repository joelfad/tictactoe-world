package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketPasswordChange;
import me.benthomas.tttworld.net.PacketPasswordChangeResult;
import me.benthomas.tttworld.net.PacketPasswordChangeResult.Result;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Account;

public class PasswordChangeHandler implements PacketHandler<PacketPasswordChange> {
    private TTTWClientConnection client;
    
    public PasswordChangeHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketPasswordChange packet) throws IOException {
        if (packet.getPassword().length() < 8) {
            this.client.sendPacket(new PacketPasswordChangeResult(Result.PASSWORD_SHORT));
        } else if (packet.getUsername() != null) {
            if (!this.client.getAccount().isAdmin()) {
                this.client.sendPacket(new PacketPasswordChangeResult(Result.NOT_ALLOWED));
            } else {
                Account a = this.client.getServer().getAccountManager().getAccount(packet.getUsername());
                
                if (a != null) {
                    a.setPassword(packet.getPassword());
                    this.client.sendPacket(new PacketPasswordChangeResult(Result.OK));
                } else {
                    this.client.sendPacket(new PacketPasswordChangeResult(Result.USER_NOT_FOUND));
                }
            }
        } else {
            Account a = this.client.getAccount();
            a.setPassword(packet.getPassword());
            
            this.client.sendPacket(new PacketPasswordChangeResult(Result.OK));
        }
    }
    
}
