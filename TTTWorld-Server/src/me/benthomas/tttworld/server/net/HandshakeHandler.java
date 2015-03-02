package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketAuthenticate;
import me.benthomas.tttworld.net.PacketClientHandshake;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.PacketServerHandshake;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Server;

public final class HandshakeHandler implements PacketHandler<PacketClientHandshake> {
    private final TTTWClientConnection client;
    
    public HandshakeHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketClientHandshake packet) throws IOException {
        if (packet.getMajorProtocolVersion() == TTTWConnection.PROTOCOL_MAJOR_VERSION) {
            Server s = this.client.getServer();
            
            this.client.sendPacket(new PacketServerHandshake(s.getCompressionThreshold(), s.getName(), this.client.getServer()
                    .isRegistrationAllowed(), s.getEncodedPublicKey()));
            
            this.client.setHandler(PacketClientHandshake.class, null);
            this.client.setHandler(PacketAuthenticate.class, new AuthenticateHandler(this.client));
            this.client.setHandler(PacketRegister.class, new RegisterHandler(this.client));
        } else {
            this.client.disconnect("Protocol version mismatch");
        }
    }
    
}
