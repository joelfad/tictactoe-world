package me.benthomas.tttworld.server.net;

import java.io.IOException;
import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.Crypto.CryptoException;
import me.benthomas.tttworld.net.PacketStartEncrypt;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

/**
 * A class capable of handling client attempts to begin encrypting
 * communications sent as a {@link PacketStartEncrypt}. Begins encrypting
 * communications to the given client and automatically unregisters itself.
 *
 * @author Ben Thomas
 */
public class StartEncryptHandler implements PacketHandler<PacketStartEncrypt> {
    private TTTWClientConnection client;
    
    /**
     * Creates a new encryption request handler for the given client.
     * 
     * @param client The client for which this handler should handle encryption
     *            requests.
     */
    public StartEncryptHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketStartEncrypt packet) throws IOException {
        try {
            this.client.setEncryptionKey(Crypto.decryptAsymmetric(packet.getCryptKey(), this.client.getServer().getKeyPair()
                    .getPrivate()));
            this.client.setDefaultHandler(PacketStartEncrypt.class, null);
        } catch (CryptoException e) {
            this.client.disconnect("Unable to read encryption key!");
        }
    }
    
}
