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
    
    public Player getPlayerX() {
        return this.xPlayer;
    }
    
    public Player getPlayerO() {
        return this.oPlayer;
    }
    
    public Player getActivePlayer() {
        return (this.turn) ? this.oPlayer : this.xPlayer;
    }
    
    public Player getInactivePlayer() {
        return (this.turn) ? this.xPlayer : this.oPlayer;
    }
    
    public boolean isDone() {
        return this.done;
    }
    
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
