package me.benthomas.tttworld.client.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.Box;

import java.awt.Component;

import me.benthomas.tttworld.net.PacketChallenge;
import me.benthomas.tttworld.net.PacketChallengeCancel;
import me.benthomas.tttworld.net.PacketChallengeResponse;
import me.benthomas.tttworld.net.TTTWConnection.PacketFilter;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ChallengeDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private MainFrame parent;
    private ChallengeCancelHandler cancelHandler;
    
    private PacketChallenge packet;
    private long start;
    private Runnable onClose;
    
    private Timer timer;
    
    private JPanel contentPane;
    private JPanel panel;
    private JButton btnYes;
    private JPanel panel_1;
    private JLabel lblThisChallengeWill;
    private JLabel lblseconds;
    private JLabel lblSeconds;
    private JButton btnNo;
    private Box verticalBox;
    private JLabel lblYouHaveBeen;
    private JLabel lblplayer;
    private JLabel lblWillYouAccept;
    
    public ChallengeDialog(MainFrame parent, PacketChallenge packet) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (ChallengeDialog.this.packet != null && ChallengeDialog.this.parent.getServer() != null) {
                    ChallengeDialog.this.parent.getServer().sendPacket(
                            new PacketChallengeResponse(ChallengeDialog.this.packet.getChallengeId(),
                                    PacketChallengeResponse.Response.REJECT));
                }
            }
        });
        setAlwaysOnTop(true);
        
        this.parent = parent;
        this.cancelHandler = new ChallengeCancelHandler();
        parent.getServer().addFilteredHandler(PacketChallengeCancel.class, new ChallengeCancelFilter(), this.cancelHandler);
        
        this.packet = packet;
        this.start = System.currentTimeMillis();
        
        setResizable(false);
        setTitle("Challenge Received");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 344, 151);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        panel = new JPanel();
        contentPane.add(panel, BorderLayout.SOUTH);
        
        btnYes = new JButton("Yes");
        btnYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChallengeDialog.this.parent.getServer().sendPacket(
                        new PacketChallengeResponse(ChallengeDialog.this.packet.getChallengeId(),
                                PacketChallengeResponse.Response.ACCEPT));
                ChallengeDialog.this.packet = null;
                
                ChallengeDialog.this.close();
            }
        });
        panel.add(btnYes);
        
        btnNo = new JButton("No");
        btnNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChallengeDialog.this.close();
            }
        });
        panel.add(btnNo);
        
        panel_1 = new JPanel();
        contentPane.add(panel_1, BorderLayout.CENTER);
        
        lblThisChallengeWill = new JLabel("This challenge will expire in");
        lblThisChallengeWill.setFont(new Font("Dialog", Font.PLAIN, 12));
        panel_1.add(lblThisChallengeWill);
        
        lblseconds = new JLabel("%SECONDS%");
        lblseconds.setFont(new Font("Tahoma", Font.BOLD, 11));
        panel_1.add(lblseconds);
        
        this.updateTimer();
        
        lblSeconds = new JLabel("seconds.");
        lblSeconds.setFont(new Font("Dialog", Font.PLAIN, 12));
        panel_1.add(lblSeconds);
        
        verticalBox = Box.createVerticalBox();
        contentPane.add(verticalBox, BorderLayout.NORTH);
        
        lblYouHaveBeen = new JLabel("You have been challenged to a game of Tic-Tac-Toe by");
        lblYouHaveBeen.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblYouHaveBeen.setAlignmentX(Component.CENTER_ALIGNMENT);
        verticalBox.add(lblYouHaveBeen);
        
        lblplayer = new JLabel(packet.getSender().username);
        lblplayer.setAlignmentX(Component.CENTER_ALIGNMENT);
        verticalBox.add(lblplayer);
        
        lblWillYouAccept = new JLabel("Will you accept your destiny and duel to the death?");
        lblWillYouAccept.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblWillYouAccept.setAlignmentX(Component.CENTER_ALIGNMENT);
        verticalBox.add(lblWillYouAccept);
        
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChallengeDialog.this.updateTimer();
            }
        });
        timer.start();
    }
    
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
    
    public void close() {
        this.dispose();
        this.timer.stop();
        
        if (this.onClose != null) {
            this.onClose.run();
        }
    }
    
    public void updateTimer() {
        long timeLeft = this.packet.getTimeout() - (System.currentTimeMillis() - this.start);
        
        if (timeLeft > 0) {
            lblseconds.setText(String.valueOf(timeLeft / 1000));
        } else {
            this.packet = null;
            this.close();
        }
    }
    
    public class ChallengeCancelFilter implements PacketFilter<PacketChallengeCancel> {
        @Override
        public boolean isFiltered(PacketChallengeCancel packet) {
            return !packet.getChallengeId().equals(ChallengeDialog.this.packet.getChallengeId());
        }
    }
    
    public class ChallengeCancelHandler implements PacketHandler<PacketChallengeCancel> {
        @Override
        public void handlePacket(PacketChallengeCancel packet) throws IOException {
            ChallengeDialog.this.packet = null;
            ChallengeDialog.this.close();
        }
    }
    
}
