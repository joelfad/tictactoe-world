package me.benthomas.tttworld.client.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JSplitPane;

import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import me.benthomas.tttworld.client.net.PacketHandleThread;
import me.benthomas.tttworld.client.net.PasswordChangeHandler;
import me.benthomas.tttworld.client.net.TTTWServerConnection;
import me.benthomas.tttworld.net.PacketAuthenticate;
import me.benthomas.tttworld.net.PacketChallenge;
import me.benthomas.tttworld.net.PacketClientHandshake;
import me.benthomas.tttworld.net.PacketGameOver;
import me.benthomas.tttworld.net.PacketGameUpdate;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;
import me.benthomas.tttworld.net.PacketPasswordChange;
import me.benthomas.tttworld.net.PacketPasswordChangeResult;
import me.benthomas.tttworld.net.PacketRegister;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.net.TTTWConnection.DisconnectListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextPane;

import java.awt.Font;

import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;
import javax.swing.JRadioButtonMenuItem;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPane;
    
    private ServerSelectDialog serverSelect;
    private LoginDialog login;
    private List<ChallengeDialog> challenges = new ArrayList<ChallengeDialog>();
    private HashMap<UUID, GameFrame> games = new HashMap<UUID, GameFrame>();
    
    private TTTWServerConnection server;
    private PacketHandleThread packetThread;
    
    private boolean registerAllowed;
    private String username;
    private boolean admin;
    
    private JDialog dialog;
    
    private JMenu mnAdmin;
    private JTextField chatField;
    private JTextPane chatPane;
    private JButton btnSend;
    
    private JList<PlayerInfo> playerList;
    private DefaultListModel<PlayerInfo> playerListModel;
    private JMenu mnAdmin_1;
    private JPopupMenu popupMenu;
    private JMenuItem mntmPromote;
    private JMenuItem mntmDemote;
    
    /**
     * Create the frame.
     */
    public MainFrame() {
        setTitle("Tic-Tac-Toe World");
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 600, 500);
        
        addWindowListener(new PartialWindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.this.disconnect();
            }
        });
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu mnGame = new JMenu("Game");
        menuBar.add(mnGame);
        
        JMenuItem mntmNewGame = new JMenuItem("New Game...");
        mnGame.add(mntmNewGame);
        
        JMenuItem mntmJoinGame = new JMenuItem("Join Game...");
        mnGame.add(mntmJoinGame);
        
        JMenuItem mntmFindGame = new JMenuItem("Find Game...");
        mnGame.add(mntmFindGame);
        
        JMenuItem mntmSpectateGame = new JMenuItem("Spectate Game...");
        mnGame.add(mntmSpectateGame);
        
        JSeparator separator = new JSeparator();
        mnGame.add(separator);
        
        JMenuItem mntmDisconnect = new JMenuItem("Disconnect");
        mntmDisconnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.disconnect();
            }
        });
        mnGame.add(mntmDisconnect);
        
        JMenu mnAccount = new JMenu("Account");
        menuBar.add(mnAccount);
        
        JMenuItem mntmChangePassword = new JMenuItem("Change Password...");
        mntmChangePassword.addActionListener(new ActionListener() {
            private CreatePasswordDialog d;
            
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.dialog = d = new CreatePasswordDialog("Change Password", MainFrame.this.username, new Runnable() {
                    @Override
                    public void run() {
                        MainFrame.this.server.setHandler(PacketPasswordChangeResult.class, new PasswordChangeHandler(
                                MainFrame.this.server, d, false));
                        MainFrame.this.server.sendPacket(new PacketPasswordChange(null, new String(d.getPasswordField()
                                .getPassword())));
                    }
                });
                
                d.setModalityType(ModalityType.DOCUMENT_MODAL);
                d.setVisible(true);
            }
        });
        
        JMenu mnReceiveChallengesFrom = new JMenu("Receive Challenges From");
        mnAccount.add(mnReceiveChallengesFrom);
        
        JRadioButtonMenuItem rdbtnmntmEverybody = new JRadioButtonMenuItem("Everybody");
        rdbtnmntmEverybody.setSelected(true);
        mnReceiveChallengesFrom.add(rdbtnmntmEverybody);
        
        JRadioButtonMenuItem rdbtnmntmFriends = new JRadioButtonMenuItem("Friends");
        mnReceiveChallengesFrom.add(rdbtnmntmFriends);
        
        JRadioButtonMenuItem rdbtnmntmNobody = new JRadioButtonMenuItem("Nobody");
        mnReceiveChallengesFrom.add(rdbtnmntmNobody);
        
        JMenuItem mntmFriendList = new JMenuItem("Friend List...");
        mnAccount.add(mntmFriendList);
        
        JSeparator separator_2 = new JSeparator();
        mnAccount.add(separator_2);
        mnAccount.add(mntmChangePassword);
        
        mnAdmin = new JMenu("Admin");
        menuBar.add(mnAdmin);
        
        JMenuItem mntmResetPassword = new JMenuItem("Reset Password...");
        mntmResetPassword.addActionListener(new ActionListener() {
            private CreatePasswordDialog d;
            
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.dialog = d = new CreatePasswordDialog("Reset Password", null, new Runnable() {
                    @Override
                    public void run() {
                        MainFrame.this.server.setHandler(PacketPasswordChangeResult.class, new PasswordChangeHandler(
                                MainFrame.this.server, d, true));
                        MainFrame.this.server.sendPacket(new PacketPasswordChange(d.getUsernameField().getText(), new String(d
                                .getPasswordField().getPassword())));
                    }
                });
                
                d.setModalityType(ModalityType.DOCUMENT_MODAL);
                d.setVisible(true);
            }
        });
        mnAdmin.add(mntmResetPassword);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.3);
        contentPane.add(splitPane, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        splitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout(0, 0));
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        chatPane = new JTextPane();
        scrollPane.setViewportView(chatPane);
        chatPane.setFont(new Font("Courier New", Font.PLAIN, 12));
        chatPane.setText("<aviansie_ben> Lololol");
        chatPane.setEditable(false);
        
        JPanel panel_2 = new JPanel();
        panel.add(panel_2, BorderLayout.SOUTH);
        panel_2.setLayout(new BorderLayout(0, 0));
        
        chatField = new JTextField();
        chatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(MainFrame.this.chatField.getText()));
                    MainFrame.this.chatField.setText("");
                }
            }
        });
        chatField.setFont(new Font("Courier New", Font.PLAIN, 12));
        panel_2.add(chatField);
        chatField.setColumns(10);
        
        chatField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                MainFrame.this.btnSend.setEnabled(MainFrame.this.chatField.getText().length() > 0);
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                this.changed();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                this.changed();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                this.changed();
            }
        });
        
        btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.server.sendPacket(new PacketGlobalChat(MainFrame.this.chatField.getText()));
                MainFrame.this.chatField.setText("");
            }
        });
        btnSend.setEnabled(false);
        panel_2.add(btnSend, BorderLayout.EAST);
        
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setPreferredSize(new Dimension(0, 0));
        splitPane.setLeftComponent(scrollPane_1);
        
        playerList = new JList<PlayerInfo>(playerListModel = new DefaultListModel<PlayerInfo>());
        scrollPane_1.setViewportView(playerList);
        playerList.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
            
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
            
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (playerList.getSelectedIndex() == -1) {
                    popupMenu.setEnabled(false);
                } else {
                    popupMenu.setEnabled(true);
                    
                    if (playerList.getSelectedValue().admin) {
                        mntmPromote.setVisible(false);
                        mntmDemote.setVisible(true);
                    } else {
                        mntmPromote.setVisible(true);
                        mntmDemote.setVisible(false);
                    }
                }
            }
        });
        addPopup(playerList, popupMenu);
        
        JMenuItem mntmChallenge = new JMenuItem("Challenge");
        mntmChallenge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerList.getSelectedValue() != null) {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(":challenge " + playerList.getSelectedValue().username));
                }
            }
        });
        popupMenu.add(mntmChallenge);
        
        mnAdmin_1 = new JMenu("Admin");
        popupMenu.add(mnAdmin_1);
        
        JMenuItem mntmKick = new JMenuItem("Kick");
        mntmKick.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerList.getSelectedValue() != null) {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(":kick " + playerList.getSelectedValue().username));
                }
            }
        });
        mnAdmin_1.add(mntmKick);
        
        JMenuItem mntmBan = new JMenuItem("Ban");
        mntmBan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerList.getSelectedValue() != null) {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(":ban " + playerList.getSelectedValue().username));
                }
            }
        });
        mnAdmin_1.add(mntmBan);
        
        JSeparator separator_1 = new JSeparator();
        mnAdmin_1.add(separator_1);
        
        mntmPromote = new JMenuItem("Promote");
        mntmPromote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerList.getSelectedValue() != null) {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(":promote " + playerList.getSelectedValue().username));
                }
            }
        });
        mnAdmin_1.add(mntmPromote);
        
        mntmDemote = new JMenuItem("Demote");
        mntmDemote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerList.getSelectedValue() != null) {
                    MainFrame.this.server.sendPacket(new PacketGlobalChat(":demote " + playerList.getSelectedValue().username));
                }
            }
        });
        mnAdmin_1.add(mntmDemote);
    }
    
    public boolean isRegistrationAllowed() {
        return this.registerAllowed;
    }
    
    public void setRegistrationAllowed(boolean registerAllowed) {
        this.registerAllowed = registerAllowed;
    }
    
    public boolean isAdmin() {
        return this.admin;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
        
        this.mnAdmin.setVisible(admin);
        this.mnAdmin_1.setVisible(admin);
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public TTTWServerConnection getServer() {
        return this.server;
    }
    
    public LoginDialog getLoginDialog() {
        return this.login;
    }
    
    private void onDisconnect() {
        this.packetThread.interrupt();
        this.packetThread = null;
        
        this.server = null;
        
        this.setVisible(false);
        
        if (this.login != null) {
            this.login.close();
            this.login = null;
        }
        
        if (this.dialog != null) {
            this.dialog.dispose();
            this.dialog = null;
        }
        
        for (ChallengeDialog challenge : this.challenges) {
            challenge.close();
        }
        
        this.challenges.clear();
        
        for (Entry<UUID, GameFrame> game : this.games.entrySet()) {
            game.getValue().dispose();
        }
        
        this.games.clear();
    }
    
    public void disconnect() {
        if (this.server != null) {
            this.server.removeDisconnectListener(new DisconnectNotifier());
            this.server.disconnect("User Disconnected");
            
            this.onDisconnect();
        }
        
        this.setVisible(false);
        this.displayServerSelectDialog();
    }
    
    public void connectToServer(String host, int port) {
        try {
            Socket s = new Socket(host, port);
            
            this.server = new TTTWServerConnection(s, this, host);
            this.server.addDisconnectListener(new DisconnectNotifier());
            this.server.sendPacket(new PacketClientHandshake(TTTWConnection.PROTOCOL_MAJOR_VERSION,
                    TTTWConnection.PROTOCOL_MINOR_VERSION));
            
            this.packetThread = new PacketHandleThread(this.server);
            this.packetThread.start();
        } catch (IOException e) {
            if (this.server != null) {
                this.server.close();
            }
            
            JOptionPane.showMessageDialog(this, "Failed to connect to the server!", "Error", JOptionPane.ERROR_MESSAGE);
            this.displayServerSelectDialog();
        }
    }
    
    public void sendCredentials(String username, String password) {
        this.server.sendPacket(new PacketAuthenticate(username, password));
    }
    
    public void sendRegistration(String username, String password) {
        this.server.sendPacket(new PacketRegister(username, password));
    }
    
    public void displayServerSelectDialog() {
        if (this.serverSelect == null) {
            this.serverSelect = new ServerSelectDialog(this);
            
            this.serverSelect.addWindowListener(new PartialWindowListener() {
                @Override
                public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
        }
        
        this.serverSelect.setVisible(true);
    }
    
    public void displayLoginDialog() {
        if (this.login == null) {
            this.login = new LoginDialog(this);
            
            this.login.addWindowListener(new PartialWindowListener() {
                @Override
                public void windowClosed(WindowEvent e) {
                    MainFrame.this.disconnect();
                }
            });
        }
        
        this.login.getUsernameField().setText("");
        this.login.getPasswordField().setText("");
        this.login.getNewAccountButton().setEnabled(this.registerAllowed);
        
        this.login.setVisible(true);
        this.login.getUsernameField().grabFocus();
    }
    
    public void displayMainFrame() {
        this.chatPane.setText("");
        this.setVisible(true);
    }
    
    public void displayChallengeDialog(PacketChallenge p) {
        ChallengeDialog d = new ChallengeDialog(this, p);
        d.setOnClose(new Runnable() {
            @Override
            public void run() {
                MainFrame.this.challenges.remove(d);
            }
        });
        d.setVisible(true);
    }
    
    public void handleGameUpdate(PacketGameUpdate p) {
        if (this.games.containsKey(p.getGameId())) {
            this.games.get(p.getGameId()).handlePacket(p);
        } else {
            GameFrame f = new GameFrame(this, p);
            f.setVisible(true);
            
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    MainFrame.this.games.remove(p.getGameId());
                }
            });
            
            this.games.put(p.getGameId(), f);
        }
    }
    
    public void handleGameOver(PacketGameOver p) {
        if (this.games.containsKey(p.getGameId())) {
            this.games.get(p.getGameId()).handlePacket(p);
        }
    }
    
    private class DisconnectNotifier implements DisconnectListener {
        @Override
        public void onDisconnect(boolean fromRemote, String reason) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainFrame.this.onDisconnect();
                    
                    JOptionPane
                            .showMessageDialog(null, "Disconnected from server: " + reason, "Error", JOptionPane.ERROR_MESSAGE);
                    MainFrame.this.displayServerSelectDialog();
                }
            });
        }
        
        @Override
        public boolean equals(Object o) {
            return (o instanceof DisconnectNotifier);
        }
    }
    
    public interface PartialWindowListener extends WindowListener {
        @Override
        public default void windowActivated(WindowEvent e) {
        }
        
        @Override
        public default void windowClosed(WindowEvent e) {
        }
        
        @Override
        public default void windowClosing(WindowEvent e) {
        }
        
        @Override
        public default void windowDeactivated(WindowEvent e) {
        }
        
        @Override
        public default void windowDeiconified(WindowEvent e) {
        }
        
        @Override
        public default void windowIconified(WindowEvent e) {
        }
        
        @Override
        public default void windowOpened(WindowEvent e) {
        }
    }
    
    public JTextPane getChatPane() {
        return chatPane;
    }
    
    public DefaultListModel<PlayerInfo> getPlayerListModel() {
        return playerListModel;
    }
    
    private void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    playerList.setSelectedIndex(playerList.locationToIndex(e.getPoint()));
                    
                    if (playerList.getSelectedIndex() != -1) {
                        showMenu(e);
                    }
                }
            }
            
            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
}
