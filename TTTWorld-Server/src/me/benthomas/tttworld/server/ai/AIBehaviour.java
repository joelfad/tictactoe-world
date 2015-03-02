package me.benthomas.tttworld.server.ai;

/**
 * Represents a type of behaviour that an AI player can exhibit.
 * 
 * @author Ben Thomas
 */
public interface AIBehaviour {

    /**
     * Attempts to make a move based on this behaviour. If a move based on this
     * behaviour can be made, the move is performed and {@code true} is
     * returned. Otherwise, no move is made and {@code false} is returned.
     * 
     * @param p The player for whom the move should be made.
     * @return {@code true} if a move was successfully made.
     */
    public boolean tryMakeMove(AIPlayer p);

    /**
     * Represents coordinates on a tic-tac-toe board.
     * 
     * @author Ben Thomas
     */
    public class Point {

        /**
         * The x coordinate (column) of this point.
         */
        public final int x;

        /**
         * The y coordinate (row) of this point.
         */
        public final int y;

        /**
         * Creates a new point at the given coordinates.
         * 
         * @param x The x coordinate (column) of this point. Should be between 0
         *            and 2, inclusive.
         * @param y The y coordinate (row) of this point. Should be between 0
         *            and 2, inclusive.
         */
        public Point(int x, int y) {
            if (x < 0 || x >= 3) {
                throw new IllegalArgumentException("x coordinate out of bounds!");
            } else if (y < 0 || y >= 3) {
                throw new IllegalArgumentException("y coordinate out of bounds!");
            }

            this.x = x;
            this.y = y;
        }
    }
}
