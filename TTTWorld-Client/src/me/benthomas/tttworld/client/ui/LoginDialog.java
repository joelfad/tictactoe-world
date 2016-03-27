package me.benthomas.tttworld.client.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

public class LoginDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private MainFrame parent;
    
    private CreatePasswordDialog register;
    
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel buttonPanel;
    private JButton btnNewAccount;
    
    public LoginDialog(MainFrame parent) {
        setAlwaysOnTop(true);
        this.parent = parent;
        
        setResizable(false);
        setTitle("Connect to Server...");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 274, 137);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));
        
        JLabel lblUsername = new JLabel("Username:");
        contentPane.add(lblUsername, "2, 2, right, default");
        
        usernameField = new JTextField();
        contentPane.add(usernameField, "4, 2, fill, default");
        usernameField.setColumns(10);
        
        JLabel lblPassword = new JLabel("Password:");
        contentPane.add(lblPassword, "2, 4, right, default");
        
        passwordField = new JPasswordField();
        contentPane.add(passwordField, "4, 4, fill, default");
        passwordField.setColumns(10);
        
        buttonPanel = new JPanel();
        contentPane.add(buttonPanel, "4, 6, right, fill");
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        btnNewAccount = new JButton("New Account");
        btnNewAccount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.register = new CreatePasswordDialog("Register New Account", null, new Runnable() {
                    @Override
                    public void run() {
                        LoginDialog.this.parent.sendRegistration(LoginDialog.this.register.getUsernameField().getText(),
                                new String(LoginDialog.this.register.getPasswordField().getPassword()));
                    }
                });
                
                LoginDialog.this.register.setModalityType(ModalityType.DOCUMENT_MODAL);
                LoginDialog.this.register.setVisible(true);
            }
        });
        buttonPanel.add(btnNewAccount);
        
        JButton btnOk = new JButton("OK");
        buttonPanel.add(btnOk);
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.parent.sendCredentials(LoginDialog.this.usernameField.getText(), new String(
                        LoginDialog.this.passwordField.getPassword()));
            }
        });
        
        SwingUtilities.getRootPane(btnOk).setDefaultButton(btnOk);
    }
    
    public JTextField getUsernameField() {
        return usernameField;
    }
    
    public JPasswordField getPasswordField() {
        return passwordField;
    }
    
    public JButton getNewAccountButton() {
        return btnNewAccount;
    }
    
    public void close() {
        this.setVisible(false);
        
        if (this.register != null) {
            this.register.dispose();
            this.register = null;
        }
    }
}
