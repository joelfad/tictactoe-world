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
import me.benthomas.tttworld.net.TTTWConnection.PacketFilter;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

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
    private JTextPane textPane;
    private JScrollPane scrollPane;
    
    private MainFrame parent;
    private UUID gameId;
    private Board board;
    private boolean myTurn;
    
    private GameUpdateHandler updateHandler;
    private GameOverHandler overHandler;
    
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
        
        this.updateHandler = new GameUpdateHandler();
        this.overHandler = new GameOverHandler();
        
        parent.getServer().addFilteredHandler(PacketGameUpdate.class, new GameUpdateFilter(), this.updateHandler);
        parent.getServer().addFilteredHandler(PacketGameOver.class, new GameOverFilter(), this.overHandler);
        
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
        
        this.updateHandler.handlePacket(p);
    }
    
    public class GameUpdateFilter implements PacketFilter<PacketGameUpdate> {
        @Override
        public boolean isFiltered(PacketGameUpdate packet) {
            return !packet.getGameId().equals(GameFrame.this.gameId);
        }
    }
    
    public class GameUpdateHandler implements PacketHandler<PacketGameUpdate> {
        @Override
        public void handlePacket(PacketGameUpdate p) {
            GameFrame.this.board = p.getBoard();
            GameFrame.this.myTurn = p.isYourTurn();
            
            GameFrame.this.panel.setBoard(board);
            
            if (!p.isGameOver()) {
                if (p.isYourTurn()) {
                    GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "It's your turn!\n");
                } else {
                    GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "Waiting for opponent...\n");
                }
            }
        }
    }
    
    public class GameOverFilter implements PacketFilter<PacketGameOver> {
        @Override
        public boolean isFiltered(PacketGameOver packet) {
            return !packet.getGameId().equals(GameFrame.this.gameId);
        }
    }
    
    public class GameOverHandler implements PacketHandler<PacketGameOver> {
        @Override
        public void handlePacket(PacketGameOver p) {
            if (p.getResult() == Result.LOST) {
                GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "You lost!\n");
            } else if (p.getResult() == Result.WON) {
                GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "You won!\n");
            } else if (p.getResult() == Result.DRAWN) {
                GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "It's a draw! Everybody loses!\n");
            } else {
                GameFrame.this.textPane.setText(GameFrame.this.textPane.getText() + "Something happened?\n");
            }
            
            GameFrame.this.parent.getServer().removeFilteredHandler(PacketGameUpdate.class, GameFrame.this.updateHandler);
            GameFrame.this.parent.getServer().removeFilteredHandler(PacketGameOver.class, GameFrame.this.overHandler);
            
            GameFrame.this.gameId = null;
        }
    }
    
}
