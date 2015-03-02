package me.benthomas.tttworld.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import me.benthomas.tttworld.Mark;
import me.benthomas.tttworld.net.PacketChallenge;
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
    
    public synchronized void rejectChallenge(UUID challengeId, TTTWClientConnection receiver) {
        Challenge c = this.challenges.get(challengeId);
        
        if (c != null && c.receiver == receiver) {
            this.challenges.remove(challengeId);
        }
    }
    
    public synchronized Game acceptChallenge(UUID challengeId, TTTWClientConnection receiver) {
        Challenge c = this.challenges.get(challengeId);
        
        if (c != null && c.receiver == receiver) {
            this.challenges.remove(challengeId);
            
            return this.createGame(challengeId, new NetPlayer(receiver, Mark.X), new NetPlayer(c.sender, Mark.O));
        } else {
            return null;
        }
    }
    
    public synchronized Game createGame(UUID gameId, Player xPlayer, Player oPlayer) {
        Game g = new Game(gameId, xPlayer, oPlayer);
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
        
        public Challenge(UUID id, long expires, TTTWClientConnection sender, TTTWClientConnection receiver) {
            this.id = id;
            this.expires = expires;
            
            this.sender = sender;
            this.receiver = receiver;
        }
    }
    
}
