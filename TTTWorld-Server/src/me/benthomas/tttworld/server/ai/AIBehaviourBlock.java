package me.benthomas.tttworld.server.ai;

/**
 * Represents an AI behaviour in which the player will attempt to block an
 * opposing player from making a winning move.
 * 
 * @author Ben Thomas
 */
public class AIBehaviourBlock extends AIBehaviourComplete {

    @Override
    public boolean tryMakeMove(AIPlayer p) {
        Point selectedPoint = this.findPoint(p.getGame().getBoard(), p.getGame().getInactivePlayer().getMark());

        if (selectedPoint != null) {
            p.getGame().getBoard().setMark(selectedPoint.x, selectedPoint.y, p.getMark());
            return true;
        } else {
            return false;
        }
    }

}
