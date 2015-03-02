package me.benthomas.tttworld.server;

import me.benthomas.tttworld.Mark;

/**
 * Represents a player of a tic-tac-toe game.
 * 
 * @author Ben Thomas
 */
public abstract class Player {
    protected final String name;
    protected final Mark mark;
    
    protected Game game;
    
    /**
     * Creates a new player with the given name, using the given mark.
     * 
     * @param name The name of the player.
     * @param mark the mark with which the player should play.
     */
    public Player(String name, Mark mark) {
        this.name = name;
        this.mark = mark;
    }
    
    /**
     * Causes the player to make a move on their board. A board must be set
     * before a move can be made.
     * 
     * @throws UnsupportedOperationException If a board has not yet been set.
     */
    public abstract void notifyUpdate(boolean myTurn);
    
    public abstract void notifyWon();
    public abstract void notifyLost();
    public abstract void notifyDrawn();
    
    public final void setGame(Game game) {
        this.game = game;
    }
    
    /**
     * Gets the name that should be displayed when referring to this player.
     * 
     * @return The name by which this player should be referred.
     */
    public final String getName() {
        return this.name;
    }
    
    /**
     * Gets the mark that this player is playing with.
     * 
     * @return The mark that this player will place on the tic-tac-toe board
     */
    public final Mark getMark() {
        return this.mark;
    }
    
    /**
     * Gets the board on which this player is currently playing.
     * 
     * @return The board on which this player is making their moves.
     */
    public final Game getGame() {
        return this.game;
    }
}
