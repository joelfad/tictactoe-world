package me.benthomas.tttworld.server.ai;

import me.benthomas.tttworld.Board;
import me.benthomas.tttworld.Mark;

/**
 * Represents an AI behaviour in which the player will attempt to complete a
 * line. This class is generalized and will complete lines of a given mark,
 * without regard to who is playing the given mark.
 * 
 * @author Ben Thomas
 */
public abstract class AIBehaviourComplete implements AIBehaviour {

    /**
     * Finds a point which will complete a line of the given mark.
     * 
     * @param b The board on which to find a valid point.
     * @param completeMark The mark for which a line should be completed.
     * @return The point at which the line can be completed, or {@code null} if
     *         no line can be completed.
     */
    protected Point findPoint(Board b, Mark completeMark) {
        Point p;

        for (int row = 0; row < 3; row++) {
            p = this.checkRow(b, completeMark, row);

            if (p != null) {
                return p;
            }
        }

        for (int col = 0; col < 3; col++) {
            p = this.checkColumn(b, completeMark, col);

            if (p != null) {
                return p;
            }
        }

        if (b.getMark(0, 0) == completeMark && b.getMark(1, 1) == completeMark && !b.getMark(2, 2).isPlayer) {
            return new Point(2, 2);
        } else if (b.getMark(0, 0) == completeMark && b.getMark(2, 2) == completeMark && !b.getMark(1, 1).isPlayer) {
            return new Point(1, 1);
        } else if (b.getMark(1, 1) == completeMark && b.getMark(2, 2) == completeMark && !b.getMark(0, 0).isPlayer) {
            return new Point(0, 0);
        }

        if (b.getMark(2, 0) == completeMark && b.getMark(1, 1) == completeMark && !b.getMark(0, 2).isPlayer) {
            return new Point(0, 2);
        } else if (b.getMark(2, 0) == completeMark && b.getMark(0, 2) == completeMark && !b.getMark(1, 1).isPlayer) {
            return new Point(1, 1);
        } else if (b.getMark(1, 1) == completeMark && b.getMark(0, 2) == completeMark && !b.getMark(2, 0).isPlayer) {
            return new Point(2, 0);
        }

        return null;
    }

    private Point checkRow(Board b, Mark completeMark, int row) {
        if (b.getMark(0, row) == completeMark && b.getMark(1, row) == completeMark && !b.getMark(2, row).isPlayer) {
            return new Point(2, row);
        } else if (b.getMark(0, row) == completeMark && b.getMark(2, row) == completeMark && !b.getMark(1, row).isPlayer) {
            return new Point(1, row);
        } else if (b.getMark(1, row) == completeMark && b.getMark(2, row) == completeMark && !b.getMark(0, row).isPlayer) {
            return new Point(0, row);
        } else {
            return null;
        }
    }

    private Point checkColumn(Board b, Mark completeMark, int col) {
        if (b.getMark(col, 0) == completeMark && b.getMark(col, 1) == completeMark && !b.getMark(col, 2).isPlayer) {
            return new Point(col, 2);
        } else if (b.getMark(col, 0) == completeMark && b.getMark(col, 2) == completeMark && !b.getMark(col, 1).isPlayer) {
            return new Point(col, 1);
        } else if (b.getMark(col, 1) == completeMark && b.getMark(col, 2) == completeMark && !b.getMark(col, 0).isPlayer) {
            return new Point(col, 0);
        } else {
            return null;
        }
    }
}
