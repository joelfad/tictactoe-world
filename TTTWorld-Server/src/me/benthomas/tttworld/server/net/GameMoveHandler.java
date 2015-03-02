package me.benthomas.tttworld.server.net;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGameMove;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.Game;
import me.benthomas.tttworld.server.NetPlayer;

public class GameMoveHandler implements PacketHandler<PacketGameMove> {
    private TTTWClientConnection client;
    
    public GameMoveHandler(TTTWClientConnection client) {
        this.client = client;
    }
    
    @Override
    public void handlePacket(PacketGameMove packet) throws IOException {
        Game g = client.getServer().getGameManager().getGame(packet.getGameId());
        
        if (g != null && !g.isDone() && (g.getActivePlayer() instanceof NetPlayer)
                && ((NetPlayer) g.getActivePlayer()).getClient() == this.client) {
            ((NetPlayer) g.getActivePlayer()).handlePacket(packet, true);
        } else if (g != null && !g.isDone() && (g.getInactivePlayer() instanceof NetPlayer)
                && ((NetPlayer) g.getInactivePlayer()).getClient() == this.client) {
            ((NetPlayer) g.getInactivePlayer()).handlePacket(packet, false);
        }
    }
    
}
