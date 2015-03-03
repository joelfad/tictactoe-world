package me.benthomas.tttworld.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import me.benthomas.tttworld.Mark;
import me.benthomas.tttworld.net.PacketChallenge;
import me.benthomas.tttworld.net.PacketChallengeCancel;
import me.benthomas.tttworld.net.PacketChallengeResponse;
import me.benthomas.tttworld.net.PacketChallengeResponse.Response;
import me.benthomas.tttworld.net.TTTWConnection.DisconnectListener;
import me.benthomas.tttworld.net.TTTWConnection.PacketFilter;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

public class GameManager {
    private long challengeTimeout;
    
    private HashMap<UUID, Challenge> challenges = new HashMap<UUID, Challenge>();
    private HashMap<UUID, Game> games = new HashMap<UUID, Game>();
    
    public GameManager(long challengeTimeout) {
        this.challengeTimeout = challengeTimeout;
    }
    
    public synchronized Challenge sendChallenge(TTTWClientConnection sender, TTTWClientConnection receiver) {
        try {
            Challenge c = new Challenge(UUID.randomUUID(), System.currentTimeMillis() + this.challengeTimeout + 1000, sender,
                    receiver);
            
            receiver.sendPacket(new PacketChallenge(c.id, sender.getAccount().toPlayerInfo(), this.challengeTimeout));
            this.challenges.put(c.id, c);
            
            return c;
        } catch (IOException e) {
            receiver.disconnect("Failed to send packet!");
            return null;
        }
    }
    
    public synchronized boolean doesChallengeExist(TTTWClientConnection sender, TTTWClientConnection receiver) {
        for (Entry<UUID, Challenge> c : this.challenges.entrySet()) {
            if (c.getValue().sender == sender && c.getValue().receiver == receiver) {
                return true;
            }
        }
        
        return false;
    }
    
    public synchronized void rejectChallenge(UUID challengeId) {
        Challenge c = this.challenges.get(challengeId);
        
        if (c != null) {
            this.challenges.remove(challengeId);
        }
    }
    
    public synchronized Game acceptChallenge(UUID challengeId) {
        Challenge c = this.challenges.get(challengeId);
        
        if (c != null) {
            c.stop(false);
            this.challenges.remove(challengeId);
            
            return this.createGame(challengeId, new NetPlayer(c.receiver, Mark.X), new NetPlayer(c.sender, Mark.O));
        } else {
            return null;
        }
    }
    
    public synchronized Game createGame(UUID gameId, Player player1, Player player2) {
        Game g;
        
        if (player1.getMark() == Mark.X && player2.getMark() == Mark.O) { 
            g = new Game(gameId, player1, player2);
        } else if (player1.getMark() == Mark.O && player2.getMark() == Mark.X) {
            g = new Game(gameId, player2, player1);
        } else {
            throw new IllegalArgumentException("Two players playing with the same mark?");
        }
        
        
        this.games.put(gameId, g);
        
        g.tick();
        
        return g;
    }
    
    public synchronized Game getGame(UUID gameId) {
        return this.games.get(gameId);
    }
    
    public synchronized void tick() {
        Iterator<Entry<UUID, Challenge>> challengeIterator = this.challenges.entrySet().iterator();
        
        while (challengeIterator.hasNext()) {
            Challenge c = challengeIterator.next().getValue();
            
            if (System.currentTimeMillis() > c.expires || !c.sender.isAlive() || !c.receiver.isAlive()) {
                c.stop(true);
                challengeIterator.remove();
            }
        }
        
        Iterator<Entry<UUID, Game>> gameIterator = this.games.entrySet().iterator();
        
        while (gameIterator.hasNext()) {
            Game g = gameIterator.next().getValue();
            
            if (g.isDone()) {
                gameIterator.remove();
            }
        }
    }
    
    public class Challenge {
        public final UUID id;
        public final long expires;
        
        public final TTTWClientConnection sender;
        public final TTTWClientConnection receiver;
        
        private boolean stopped = false;
        private ChallengeDisconnectListener disconnectListener;
        private ChallengeResponseHandler responseHandler;
        
        public Challenge(UUID id, long expires, TTTWClientConnection sender, TTTWClientConnection receiver) {
            this.id = id;
            this.expires = expires;
            
            this.sender = sender;
            this.receiver = receiver;
            
            this.disconnectListener = new ChallengeDisconnectListener();
            this.responseHandler = new ChallengeResponseHandler();
            
            this.sender.addDisconnectListener(this.disconnectListener);
            this.receiver.addDisconnectListener(this.disconnectListener);
            
            this.receiver.addFilteredHandler(PacketChallengeResponse.class, new ChallengeResponseFilter(), this.responseHandler);
        }
        
        private synchronized void stop(boolean cancel) {
            if (!this.stopped) {
                this.stopped = true;
                
                this.sender.removeDisconnectListener(this.disconnectListener);
                this.receiver.removeDisconnectListener(this.disconnectListener);
                
                this.receiver.removeFilteredHandler(PacketChallengeResponse.class, this.responseHandler);
                
                if (cancel) {
                    try {
                        this.receiver.sendPacket(new PacketChallengeCancel(Challenge.this.id));
                    } catch (IOException e) {
                        // There's not much we can do about this...
                    }
                }
            }
        }
        
        private class ChallengeDisconnectListener implements DisconnectListener {
            @Override
            public void onDisconnect(boolean fromRemote, String reason) {
                Challenge.this.stop(true);
                GameManager.this.rejectChallenge(Challenge.this.id);
            }
        }
        
        private class ChallengeResponseFilter implements PacketFilter<PacketChallengeResponse> {
            @Override
            public boolean isFiltered(PacketChallengeResponse packet) {
                return !packet.getChallengeId().equals(Challenge.this.id);
            }
        }
        
        private class ChallengeResponseHandler implements PacketHandler<PacketChallengeResponse> {
            @Override
            public void handlePacket(PacketChallengeResponse packet) {
                if (packet.getResponse() == Response.ACCEPT) {
                    GameManager.this.acceptChallenge(Challenge.this.id);
                } else {
                    GameManager.this.rejectChallenge(Challenge.this.id);
                }
            }
        }
    }
    
}
