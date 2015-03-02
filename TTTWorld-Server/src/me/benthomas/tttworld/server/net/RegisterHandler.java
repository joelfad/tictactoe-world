package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Account;

public class RegisterHandler implements PacketHandler<PacketRegister> {
    private TTTWClientConnection client;
    
    public RegisterHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketRegister packet) throws IOException {
        if (this.client.getServer().getAccountManager().getAccount(packet.getUsername()) != null) {
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.NAME_TAKEN, null, false));
        } else if (packet.getPassword().length() < 8) {
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.PASSWORD_SHORT, null, false));
        } else {
            Account a = this.client.getServer().getAccountManager().createAccount(packet.getUsername(), packet.getPassword(), false);
            
            this.client.setAccount(a);
            this.client.getServer().getAccountManager().setAccountSticky(a.getName(), true);
            this.client.sendPacket(new PacketAuthResult(PacketAuthResult.Result.OK, a.getName(), false));
            
            AuthenticateHandler.onAuthenticated(this.client);
            
            synchronized (System.out) {
                System.out.println(this.client.getAddress() + " has registered a new account");
            }
        }
    }
    
}
