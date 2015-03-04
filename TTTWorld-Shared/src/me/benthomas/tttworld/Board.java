package me.benthomas.tttworld;

import java.io.PrintStream;

/**
 * Represents a 3-by-3 tic-tac-toe board.
 * 
 * @author Ben Thomas
 */
public class Board {
    private Mark[] marks;
    private int numFreeSpaces;
    
    /**
     * Constructs a blank 3-by-3 board. The board will be filled with
     * {@link Mark#NONE} when it is created.
     */
    public Board() {
        this.marks = new Mark[3 * 3];
        this.clear();
    }
    
    /**
     * Parses a board from a given string included in a JSON-serialized packet.
     * 
     * @param jsonString The serialized string version of the board that should
     *            be decoded.
     */
    public Board(String jsonString) {
        this.marks = new Mark[3 * 3];
        
        if (jsonString.length() != this.marks.length) {
            throw new IllegalArgumentException("JSON-serialized board has improper length!");
        }
        
        for (int i = 0; i < this.marks.length; i++) {
            this.marks[i] = Mark.getByChar(jsonString.charAt(i));
        }
    }
    
    private void checkCoordinates(int x, int y) {
        if (x < 0 || x >= 3)
            throw new IllegalArgumentException("x coordinate out of bounds!");
        else if (y < 0 || y >= 3)
            throw new IllegalArgumentException("y coordinate out of bounds!");
    }
    
    /**
     * Gets the mark that is currently at the requested coordinates.
     * 
     * @param x The column to get the mark in. Must be between 0 and 2,
     *            inclusive.
     * @param y The row to get the mark in. Must be between 0 and 2, inclusive.
     * 
     * @return The mark that is currently placed at the requested coordinates.
     */
    public Mark getMark(int x, int y) {
        this.checkCoordinates(x, y);
        
        return this.marks[(y * 3) + x];
    }
    
    /**
     * Puts the given mark at the requested coordinates.
     * 
     * @param x The column to put the mark in. Must be between 0 and 2,
     *            inclusive.
     * @param y The row to put the mark in. Must be between 0 and 2, inclusive.
     * @param m The mark to be placed at the requested coordinates.
     */
    public void setMark(int x, int y, Mark m) {
        this.checkCoordinates(x, y);
        
        Mark oldMark = this.marks[(y * 3) + x];
        
        if (oldMark.isPlayer && !m.isPlayer)
            this.numFreeSpaces++;
        else if (!oldMark.isPlayer && m.isPlayer)
            this.numFreeSpaces--;
        
        this.marks[(y * 3) + x] = m;
    }
    
    /**
     * Checks whether the board is completely full of player marks.
     * 
     * @return True if the board is currently full, false otherwise.
     */
    public boolean isFull() {
        return this.numFreeSpaces <= 0;
    }
    
    /**
     * Checks whether the player with the given mark has won the game.
     * 
     * @param playerMark The mark of the player to check. Must be a player-type
     *            mark.
     * @return True if the given player has won, false otherwise.
     */
    public boolean hasWon(Mark playerMark) {
        if (!playerMark.isPlayer)
            throw new IllegalArgumentException("Mark is not a player mark!");
        
        // Check horizontal lines
        for (int i = 0; i < 3; i++) {
            if (this.marks[(i * 3)] == playerMark && this.marks[(i * 3) + 1] == playerMark
                    && this.marks[(i * 3) + 2] == playerMark)
                return true;
        }
        
        // Check vertical lines
        for (int i = 0; i < 3; i++) {
            if (this.marks[i] == playerMark && this.marks[i + 3] == playerMark && this.marks[i + 6] == playerMark)
                return true;
        }
        
        // Check the two diagonals
        if (this.marks[0] == playerMark && this.marks[4] == playerMark && this.marks[8] == playerMark)
            return true;
        else if (this.marks[2] == playerMark && this.marks[4] == playerMark && this.marks[6] == playerMark)
            return true;
        
        return false;
    }
    
    /**
     * Clears this board, filling all of the spaces with {@link Mark#NONE}.
     */
    public void clear() {
        for (int i = 0; i < 3 * 3; i++)
            marks[i] = Mark.NONE;
        
        numFreeSpaces = 3 * 3;
    }
    
    private void drawRow(int row, PrintStream s) {
        StringBuilder b = new StringBuilder();
        
        b.append(row);
        b.append(" |");
        
        for (int col = 0; col < 3; col++) {
            b.append("  ");
            b.append(marks[(row * 3) + col].markCharacter);
            b.append("  |");
        }
        
        s.println("  |     |     |     |");
        s.println(b.toString());
        s.println("  |     |     |     |");
        s.println("  +-----+-----+-----+");
    }
    
    /**
     * Draws this tic-tac-toe board to the given output stream.
     * 
     * @param s The stream to which the tic-tac-toe board should be drawn.
     */
    public void display(PrintStream s) {
        s.println("     0     1     2   ");
        s.println("  +-----+-----+-----+");
        
        for (int row = 0; row < 3; row++) {
            drawRow(row, s);
        }
    }
    
    /**
     * Creates a string representation of this board that can be sent in a
     * JSON-encoded packet to convey this board to other TTTW-compliant
     * endpoints.
     * 
     * @return A string representation of this board.
     */
    public String toJsonString() {
        StringBuilder b = new StringBuilder();
        
        for (int i = 0; i < this.marks.length; i++) {
            b.append(this.marks[i].markCharacter);
        }
        
        return b.toString();
    }
}
