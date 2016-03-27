package me.benthomas.tttworld.client.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ServerSelectDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private MainFrame parent;
    
    private JPanel contentPane;
    private JTextField ipField;
    private JTextField portField;
    private JButton btnOk;
    
    public ServerSelectDialog(MainFrame parent) {
        setAlwaysOnTop(true);
        this.parent = parent;
        
        setResizable(false);
        setTitle("Connect to Server...");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 261, 127);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));
        
        JLabel lblIpAddress = new JLabel("IP Address:");
        contentPane.add(lblIpAddress, "2, 2, right, default");
        
        ipField = new JTextField();
        contentPane.add(ipField, "4, 2, fill, default");
        ipField.setColumns(10);
        
        JLabel lblPort = new JLabel("Port:");
        contentPane.add(lblPort, "2, 4, right, default");
        
        btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ServerSelectDialog.this.setVisible(false);
                ServerSelectDialog.this.parent.connectToServer(ServerSelectDialog.this.ipField.getText(),
                        Integer.parseInt(ServerSelectDialog.this.portField.getText()));
            }
        });
        contentPane.add(btnOk, "1, 6, 4, 1, right, default");
        
        portField = new JTextField();
        portField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                try {
                    Integer.parseInt(portField.getText());
                    btnOk.setEnabled(true);
                } catch (NumberFormatException ex) {
                    btnOk.setEnabled(false);
                }
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
        });
        portField.setText("15060");
        contentPane.add(portField, "4, 4, fill, default");
        portField.setColumns(10);
        
        SwingUtilities.getRootPane(btnOk).setDefaultButton(btnOk);
    }
    
}
