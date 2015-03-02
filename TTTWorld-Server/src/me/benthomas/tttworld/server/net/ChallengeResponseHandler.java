package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketChallengeResponse;
import me.benthomas.tttworld.net.PacketChallengeResponse.Response;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class ChallengeResponseHandler implements PacketHandler<PacketChallengeResponse> {
    private TTTWClientConnection client;
    
    public ChallengeResponseHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketChallengeResponse packet) throws IOException {
        if (packet.getResponse() == Response.ACCEPT) {
            this.client.getServer().getGameManager().acceptChallenge(packet.getChallengeId(), this.client);
        } else {
            this.client.getServer().getGameManager().rejectChallenge(packet.getChallengeId(), this.client);
        }
    }
    
}
