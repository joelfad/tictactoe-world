package me.benthomas.tttworld.client.net;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.client.KnownHosts;
import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketServerHandshake;
import me.benthomas.tttworld.net.PacketStartEncrypt;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class HandshakeHandler implements PacketHandler<PacketServerHandshake> {
    private TTTWServerConnection server;
    
    public HandshakeHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(PacketServerHandshake packet) throws IOException {
        this.server.setDefaultHandler(PacketServerHandshake.class, null);
        this.server.getFrame().setRegistrationAllowed(packet.isRegistrationAllowed());
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (KnownHosts.doPrompt(null, HandshakeHandler.this.server.getHostName(),
                        Crypto.calculateSHA1(packet.getPublicKey()))) {
                    byte[] key = HandshakeHandler.this.server.generateEncryptionKey();
                    
                    try {
                        HandshakeHandler.this.server.sendPacket(new PacketStartEncrypt(Crypto.encryptAsymmetric(key,
                                HandshakeHandler.this.decodePublicKey(packet.getPublicKey()))));
                    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException
                            | NoSuchAlgorithmException e) {
                        HandshakeHandler.this.server.disconnect("Error securing encryption key");
                        return;
                    }
                    
                    HandshakeHandler.this.server.setEncryptionKey(key);
                    
                    if (TTTWConnection.DEBUG_NO_ENCRYPTION) {
                        JOptionPane.showMessageDialog(null, "Encryption is disabled for debugging. Be careful!", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    
                    HandshakeHandler.this.server.setDefaultHandler(PacketAuthResult.class, new AuthResultHandler(
                            HandshakeHandler.this.server));
                    HandshakeHandler.this.server.getFrame().displayLoginDialog();
                } else {
                    HandshakeHandler.this.server.disconnect("Host not trusted");
                }
            }
        });
    }
    
    private PublicKey decodePublicKey(byte[] encoded) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }
}
