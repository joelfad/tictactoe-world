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

/**
 * A class which manages games and challenges currently active on this server.
 *
 * @author Ben Thomas
 */
public class GameManager {
    private long challengeTimeout;
    
    private HashMap<UUID, Challenge> challenges = new HashMap<UUID, Challenge>();
    private HashMap<UUID, Game> games = new HashMap<UUID, Game>();
    
    GameManager(long challengeTimeout) {
        this.challengeTimeout = challengeTimeout;
    }
    
    /**
     * Creates a new challenge and notifies the receiver that they have been
     * sent a challenge.
     * 
     * @param sender The client that has sent the challenge.
     * @param receiver The client who is being challenged.
     * @return The challenge which was created.
     */
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
    
    /**
     * Checks whether there is already a challenge pending between the two
     * provided clients.
     * 
     * @param sender The client sender to check for.
     * @param receiver The client receiver to check for.
     * @return {@code true} if a challenge exists between the two provided
     *         players; otherwise, {@code false}.
     */
    public synchronized boolean doesChallengeExist(TTTWClientConnection sender, TTTWClientConnection receiver) {
        for (Entry<UUID, Challenge> c : this.challenges.entrySet()) {
            if (c.getValue().sender == sender && c.getValue().receiver == receiver) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Causes the given challenge to be rejected by the receiver.
     * 
     * @param challengeId The ID of the challenge to be rejected.
     */
    public synchronized void rejectChallenge(UUID challengeId) {
        Challenge c = this.challenges.get(challengeId);
        
        if (c != null) {
            this.challenges.remove(challengeId);
        }
    }
    
    /**
     * Causes the given challenge to be accepted by the receiver. Creates a new
     * game based on the given challenge.
     * 
     * @param challengeId The ID of the challenge that should be accepted.
     * @return The game which was created, or {@code null} if the given
     *         challenge was not found.
     */
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
    
    /**
     * Creates a new game between the two players. One of the players must be
     * configured to play with X's and the other must be configured to player
     * with O's. The game is automatically started by calling
     * {@link Game#tick()} once.
     * 
     * @param gameId The ID of the game which should be created.
     * @param player1 One of the players of this game.
     * @param player2 The other player of this game.
     * @return The newly created games.
     */
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
    
    /**
     * Gets the existing game with the given game ID.
     * 
     * @param gameId The ID of the game to be found.
     * @return The game with the given ID or {@code null} if no game exists with
     *         the given game ID.
     */
    public synchronized Game getGame(UUID gameId) {
        return this.games.get(gameId);
    }
    
    /**
     * Causes all existing challenges to be ticked and removed if they have
     * expired.
     */
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
    
    /**
     * Represents a pending challenge between two players.
     *
     * @author Ben Thomas
     */
    public class Challenge {
        
        /**
         * The unique ID of this challenge.
         */
        public final UUID id;
        
        /**
         * The millisecond at which this challenge will expire, as compared to
         * {@link System#currentTimeMillis()}.
         */
        public final long expires;
        
        /**
         * The client that sent this challenge.
         */
        public final TTTWClientConnection sender;
        
        /**
         * The client to which this challenge was sent.
         */
        public final TTTWClientConnection receiver;
        
        private boolean stopped = false;
        private ChallengeDisconnectListener disconnectListener;
        private ChallengeResponseHandler responseHandler;
        
        private Challenge(UUID id, long expires, TTTWClientConnection sender, TTTWClientConnection receiver) {
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
