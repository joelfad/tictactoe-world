package me.benthomas.tttworld.server.net;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.net.PacketStartEncrypt;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class StartEncryptHandler implements PacketHandler<PacketStartEncrypt> {
    private TTTWClientConnection client;
    
    public StartEncryptHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketStartEncrypt packet) throws IOException {
        try {
            this.client.setEncryptionKey(Crypto.decryptAsymmetric(packet.getCryptKey(), this.client.getServer().getKeyPair().getPrivate()));
            this.client.setDefaultHandler(PacketStartEncrypt.class, null);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            this.client.disconnect("Unable to read encryption key!");
        }
    }
    
}
