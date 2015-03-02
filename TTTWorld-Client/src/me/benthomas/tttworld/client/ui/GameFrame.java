package me.benthomas.tttworld.client.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import me.benthomas.tttworld.Board;
import me.benthomas.tttworld.net.PacketGameMove;
import me.benthomas.tttworld.net.PacketGameOver;
import me.benthomas.tttworld.net.PacketGameOver.Result;
import me.benthomas.tttworld.net.PacketGameUpdate;

import java.awt.Font;
import java.awt.Color;
import java.util.UUID;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;

public class GameFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPane;
    private TicTacToePanel panel;
    
    private MainFrame parent;
    private UUID gameId;
    private Board board;
    private boolean myTurn;
    private JTextPane textPane;
    private JScrollPane scrollPane;
    
    public GameFrame(MainFrame parent, PacketGameUpdate p) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (GameFrame.this.gameId != null) {
                    GameFrame.this.parent.getServer().sendPacket(new PacketGameMove(GameFrame.this.gameId, -1, -1));
                }
            }
        });
        
        this.parent = parent;
        
        this.gameId = p.getGameId();
        
        setResizable(false);
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 458, 561);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setEnabled(false);
        splitPane.setResizeWeight(0.8);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        contentPane.add(splitPane, BorderLayout.CENTER);
        
        panel = new TicTacToePanel();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (GameFrame.this.myTurn) {
                    int x = GameFrame.this.panel.getMarkX(e.getX());
                    int y = GameFrame.this.panel.getMarkY(e.getY());
                    
                    if (x >= 0 && y >= 0) {
                        GameFrame.this.parent.getServer().sendPacket(new PacketGameMove(GameFrame.this.gameId, x, y));
                    }
                }
            }
        });
        panel.setBackground(Color.WHITE);
        splitPane.setLeftComponent(panel);
        
        scrollPane = new JScrollPane();
        splitPane.setRightComponent(scrollPane);
        
        textPane = new JTextPane();
        scrollPane.setViewportView(textPane);
        textPane.setFont(new Font("Courier New", Font.PLAIN, 11));
        
        this.handlePacket(p);
    }
    
    public void handlePacket(PacketGameUpdate p) {
        this.board = p.getBoard();
        this.myTurn = p.isYourTurn();
        
        this.panel.setBoard(board);
        
        if (!p.isGameOver()) {
            if (p.isYourTurn()) {
                this.textPane.setText(this.textPane.getText() + "It's your turn!\n");
            } else {
                this.textPane.setText(this.textPane.getText() + "Waiting for opponent...\n");
            }
        }
    }
    
    public void handlePacket(PacketGameOver p) {
        if (p.getResult() == Result.LOST) {
            this.textPane.setText(this.textPane.getText() + "You lost!\n");
        } else if (p.getResult() == Result.WON) {
            this.textPane.setText(this.textPane.getText() + "You won!\n");
        } else if (p.getResult() == Result.DRAWN) {
            this.textPane.setText(this.textPane.getText() + "It's a draw! Everybody loses!\n");
        } else {
            this.textPane.setText(this.textPane.getText() + "Something happened?\n");
        }
        
        this.gameId = null;
    }
    
}
