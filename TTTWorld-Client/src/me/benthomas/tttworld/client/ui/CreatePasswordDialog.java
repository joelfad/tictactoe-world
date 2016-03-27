package me.benthomas.tttworld.client.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class CreatePasswordDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel lblConfirmPassword;
    private JPasswordField confirmPasswordField;
    
    public CreatePasswordDialog(String title, String username, final Runnable onOk) {
        setAlwaysOnTop(true);
        setResizable(false);
        setTitle(title);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 274, 158);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
        
        JLabel lblUsername = new JLabel("Username:");
        contentPane.add(lblUsername, "2, 2, right, default");
        
        usernameField = new JTextField();
        contentPane.add(usernameField, "4, 2, fill, default");
        usernameField.setColumns(10);
        
        if (username != null) {
            usernameField.setEnabled(false);
            usernameField.setText(username);
        }
        
        JLabel lblPassword = new JLabel("Password:");
        contentPane.add(lblPassword, "2, 4, right, default");
        
        passwordField = new JPasswordField();
        contentPane.add(passwordField, "4, 4, fill, default");
        passwordField.setColumns(10);
        
        lblConfirmPassword = new JLabel("Confirm Password:");
        contentPane.add(lblConfirmPassword, "2, 6, right, default");
        
        confirmPasswordField = new JPasswordField();
        contentPane.add(confirmPasswordField, "4, 6, fill, default");
        
        JButton btnOk = new JButton("OK");
        contentPane.add(btnOk, "4, 8, right, default");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword())) {
                    onOk.run();
                } else {
                    JOptionPane.showMessageDialog(CreatePasswordDialog.this, "The passwords you entered did not match!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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
    
    public JPasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }
}
