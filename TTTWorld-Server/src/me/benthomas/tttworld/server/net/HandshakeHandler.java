package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthenticate;
import me.benthomas.tttworld.net.PacketClientHandshake;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.PacketServerHandshake;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Server;

/**
 * A class which is capable of handling the beginning
 * {@link PacketClientHandshake} of the handshake process. Automatically replies
 * to the client with a {@link PacketServerHandshake} and enters the
 * authentication phase of the handshake.
 *
 * @author Ben Thomas
 */
public final class HandshakeHandler implements PacketHandler<PacketClientHandshake> {
    private final TTTWClientConnection client;
    
    /**
     * Creates a new client handshake packet handler for the given client.
     * 
     * @param client The client for which this handler should handle handshake
     *            packets.
     */
    public HandshakeHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketClientHandshake packet) throws IOException {
        if (packet.getMajorProtocolVersion() == TTTWConnection.PROTOCOL_MAJOR_VERSION) {
            Server s = this.client.getServer();
            
            this.client.sendPacket(new PacketServerHandshake(s.getCompressionThreshold(), s.getName(), this.client.getServer()
                    .isRegistrationAllowed(), s.getEncodedPublicKey()));
            
            this.client.setDefaultHandler(PacketClientHandshake.class, null);
            this.client.setDefaultHandler(PacketAuthenticate.class, new AuthenticateHandler(this.client));
            
            if (this.client.getServer().isRegistrationAllowed()) {
                this.client.setDefaultHandler(PacketRegister.class, new RegisterHandler(this.client));
            }
        } else {
            this.client.disconnect("Protocol version mismatch");
        }
    }
    
}
