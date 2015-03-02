package me.benthomas.tttworld.client.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import me.benthomas.tttworld.Board;
import me.benthomas.tttworld.Mark;

/**
 * 
 *
 * @author Ben Thomas
 */
public class TicTacToePanel extends JPanel {
    private static final int CELL_SIZE = 90;
    private static final int CELL_PADDING = 15;
    
    private static final int LINE_WIDTH = 3;
    private static final int MARK_WIDTH = 10;
    
    private static final long serialVersionUID = 1L;
    
    private Board board;

    public void setBoard(Board board) {
        this.board = board;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int w = (CELL_SIZE + CELL_PADDING * 2) * 3;
        int h = (CELL_SIZE + CELL_PADDING * 2) * 3;
        
        int x = this.getWidth() / 2 - w / 2;
        int y = this.getHeight() / 2 - h / 2;
        
        Graphics2D g2 = (Graphics2D)g;
        
        g.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(LINE_WIDTH));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int r = 1; r < 3; r++) {
            int ly = y + (CELL_SIZE + CELL_PADDING * 2) * r;
            g.drawLine(x, ly, x + w, ly);
        }
        
        for (int c = 1; c < 3; c++) {
            int lx = x + (CELL_SIZE + CELL_PADDING * 2) * c;
            g.drawLine(lx, y, lx, y + h);
        }
        
        g2.setStroke(new BasicStroke(MARK_WIDTH));
        
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int cx = x + (CELL_SIZE + CELL_PADDING * 2) * c + CELL_PADDING;
                int cy = y + (CELL_SIZE + CELL_PADDING * 2) * r + CELL_PADDING;
                
                if (board == null || board.getMark(c, r) == Mark.O) {
                    g.setColor(Color.RED);
                    g.drawOval(cx, cy, CELL_SIZE, CELL_SIZE);
                }
                
                if (board == null || board.getMark(c, r) == Mark.X) { 
                    g.setColor(Color.BLUE);
                    g.drawLine(cx, cy, cx + CELL_SIZE, cy + CELL_SIZE);
                    g.drawLine(cx + CELL_SIZE, cy, cx, cy + CELL_SIZE);
                }
            }
        }
    }
    
    public int getMarkX(int pixelX) {
        int w = (CELL_SIZE + CELL_PADDING * 2) * 3;
        int x = this.getWidth() / 2 - w / 2;
        
        if (pixelX < x || pixelX >= x + w) {
            return -1;
        } else {
            return (pixelX - x) / (CELL_SIZE + CELL_PADDING * 2);
        }
    }
    
    public int getMarkY(int pixelY) {
        int h = (CELL_SIZE + CELL_PADDING * 2) * 3;
        int y = this.getHeight() / 2 - h / 2;
        
        if (pixelY < y || pixelY >= y + h) {
            return -1;
        } else {
            return (pixelY - y) / (CELL_SIZE + CELL_PADDING * 2);
        }
    }
    
}
