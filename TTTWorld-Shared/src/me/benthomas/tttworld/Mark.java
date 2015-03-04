package me.benthomas.tttworld;

/**
 * Represents a mark in a cell of a tic-tac-toe game.
 * 
 * @author Ben Thomas
 */
public enum Mark {
    /**
     * Represents the mark that should be used for the 'O' player.
     */
    O('O', true),
    
    /**
     * Represents the mark that should be used for the 'X' player.
     */
    X('X', true),
    
    /**
     * Represents a mark where no player has yet played.
     */
    NONE(' ', false);
    
    /**
     * The character that should be displayed when this mark is being displayed
     * on the screen.
     */
    public final char markCharacter;
    
    /**
     * Whether the mark represents a cell which has been claimed by a player.
     * True if the mark is a player mark, false otherwise.
     */
    public final boolean isPlayer;
    
    private Mark(char markCharacter, boolean isPlayer) {
        this.markCharacter = markCharacter;
        this.isPlayer = isPlayer;
    }
    
    /**
     * Gets the mark associated with a specific character.
     * 
     * @param c The character to get the mark for.
     * @return The mark associated with the given character, or {@code null} if
     *         no mark is associated with that character.
     */
    public static Mark getByChar(char c) {
        for (Mark m : Mark.values()) {
            if (m.markCharacter == c) {
                return m;
            }
        }
        
        return null;
    }
}
