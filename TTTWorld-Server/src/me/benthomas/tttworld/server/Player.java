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
     * Notifies this player that the game has just started. Should be
     * immediately followed by a call to {@link #notifyUpdate(boolean)}.
     */
    public abstract void notifyStart();
    
    /**
     * Notifies this player that the board has been updated. If requested, the
     * player will be told that they must make a move.
     * 
     * @param myTurn Whether this player should make the next move.
     */
    public abstract void notifyUpdate(boolean myTurn);
    
    /**
     * Notifies this player that the game is over and that they have won.
     */
    public abstract void notifyWon();
    
    /**
     * Notifies this player that the game is over and that they have lost.
     */
    public abstract void notifyLost();
    
    /**
     * Notifies this player that the game is over and that the result of the
     * game was a draw.
     */
    public abstract void notifyDrawn();
    
    final void setGame(Game game) {
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
