package me.benthomas.tttworld.server;

import java.util.UUID;

import me.benthomas.tttworld.Board;
import me.benthomas.tttworld.Mark;

/**
 * Represents a game of tic-tac-toe.
 * 
 * @author Ben Thomas
 */
public class Game {
    private UUID id;
    private Board board;
    
    private boolean turn = true;
    private boolean done = false;
    
    private Player xPlayer;
    private Player oPlayer;
    
    /**
     * Creates a new game of tic-tac-toe, with a blank board.
     * 
     * @param id The unique ID of this game.
     * @param xPlayer The player who is playing with X's.
     * @param oPlayer The player who is playing with O's.
     */
    public Game(UUID id, Player xPlayer, Player oPlayer) {
        this.id = id;
        this.board = new Board();
        
        xPlayer.setGame(this);
        oPlayer.setGame(this);
        
        xPlayer.notifyStart();
        oPlayer.notifyStart();
        
        this.xPlayer = xPlayer;
        this.oPlayer = oPlayer;
    }
    
    /**
     * Gets the unique ID of this game.
     * 
     * @return The unique ID of this game.
     */
    public UUID getId() {
        return this.id;
    }
    
    /**
     * Gets the tic-tac-toe board that this game will be played on.
     * 
     * @return The tic-tac-toe board that this game will be played on.
     */
    public Board getBoard() {
        return this.board;
    }
    
    /**
     * Gets the player who is playing with X's.
     * 
     * @return The X player.
     */
    public Player getPlayerX() {
        return this.xPlayer;
    }
    
    /**
     * Gets the player who is playing with O's.
     * 
     * @return The O player.
     */
    public Player getPlayerO() {
        return this.oPlayer;
    }
    
    /**
     * Gets the player that this game is currently waiting on for a move.
     * 
     * @return The active player of this game.
     */
    public Player getActivePlayer() {
        return (this.turn) ? this.oPlayer : this.xPlayer;
    }
    
    /**
     * Gets the player that is not currently being waited on.
     * 
     * @return The inactive player of this game.
     */
    public Player getInactivePlayer() {
        return (this.turn) ? this.xPlayer : this.oPlayer;
    }
    
    /**
     * Gets a value indicating whether this game has been completed.
     * 
     * @return Whether this game is over.
     */
    public boolean isDone() {
        return this.done;
    }
    
    /**
     * Causes the game to move onto the next turn. If a player has won or the
     * board is full, the game is terminated; otherwise, the next player is
     * notified that it's their turn.
     */
    public synchronized void tick() {
        if (this.board.hasWon(Mark.X)) {
            this.done = true;
            this.xPlayer.notifyWon();
            this.oPlayer.notifyLost();
        } else if (this.board.hasWon(Mark.O)) {
            this.done = true;
            this.xPlayer.notifyLost();
            this.oPlayer.notifyWon();
        } else if (this.board.isFull()) {
            this.done = true;
            this.xPlayer.notifyDrawn();
            this.oPlayer.notifyDrawn();
        } else {
            this.turn = !this.turn;
            
            this.getInactivePlayer().notifyUpdate(false);
            this.getActivePlayer().notifyUpdate(true);
        }
    }
    
    /**
     * Causes the game to be cancelled with the opposing player to the provided
     * loser being the winner.
     * 
     * @param loser The player who has caused the game to be cancelled, and is
     *            thus the loser of the game.
     */
    public synchronized void cancel(Player loser) {
        this.done = true;
        
        if (this.xPlayer == loser) {
            this.xPlayer.notifyLost();
            this.oPlayer.notifyWon();
        } else {
            this.xPlayer.notifyWon();
            this.oPlayer.notifyLost();
        }
    }
}
