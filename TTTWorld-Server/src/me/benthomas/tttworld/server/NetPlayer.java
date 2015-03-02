package me.benthomas.tttworld.server;

import java.io.IOException;

import me.benthomas.tttworld.Mark;
import me.benthomas.tttworld.net.PacketGameMove;
import me.benthomas.tttworld.net.PacketGameOver;
import me.benthomas.tttworld.net.PacketGameOver.Result;
import me.benthomas.tttworld.net.PacketGameUpdate;
import me.benthomas.tttworld.net.TTTWConnection.DisconnectListener;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * 
 *
 * @author Ben Thomas
 */
public final class NetPlayer extends Player {
    private TTTWClientConnection client;
    private DisconnectForfeiter forfeit;
    
    public NetPlayer(TTTWClientConnection client, Mark mark) {
        super(client.getAccount().getName(), mark);
        
        this.client = client;
        this.forfeit = new DisconnectForfeiter();
        this.client.addDisconnectListener(this.forfeit);
    }
    
    public TTTWClientConnection getClient() {
        return this.client;
    }
    
    @Override
    public void notifyUpdate(boolean myTurn) {
        this.sendUpdate(myTurn, false);
    }
    
    private void sendUpdate(boolean myTurn, boolean gameOver) {
        try {
            this.client.sendPacket(new PacketGameUpdate(this.game.getId(), this.game.getBoard(), myTurn, gameOver));
        } catch (IOException e) {
            this.client.disconnect("Error sending packet!");
        }
    }
    
    @Override
    public void notifyWon() {
        this.notifyOver();
        
        try {
            this.client.sendPacket(new PacketGameOver(this.game.getId(), Result.WON));
        } catch (IOException e) {
            // Ignore
        }
    }
    
    @Override
    public void notifyLost() {
        this.notifyOver();
        
        try {
            this.client.sendPacket(new PacketGameOver(this.game.getId(), Result.LOST));
        } catch (IOException e) {
            // Ignore
        }
    }
    
    @Override
    public void notifyDrawn() {
        this.notifyOver();
        
        try {
            this.client.sendPacket(new PacketGameOver(this.game.getId(), Result.DRAWN));
        } catch (IOException e) {
            // Ignore
        }
    }
    
    private void notifyOver() {
        this.sendUpdate(false, true);
        this.client.removeDisconnectListener(this.forfeit);
    }
    
    public void handlePacket(PacketGameMove p, boolean active) {
        if (p.getX() == -1 && p.getY() == -1) {
            this.game.cancel(this);
        } else if (active && p.getX() >= 0 && p.getX() < 3 && p.getY() >= 0 && p.getY() < 3) {
            if (!this.game.getBoard().getMark(p.getX(), p.getY()).isPlayer) {
                this.game.getBoard().setMark(p.getX(), p.getY(), this.mark);
                this.game.tick();
            }
        }
    }
    
    public class DisconnectForfeiter implements DisconnectListener {
        @Override
        public void onDisconnect(boolean fromRemote, String reason) {
            NetPlayer.this.game.cancel(NetPlayer.this);
        }
    }
    
}
