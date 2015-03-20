package net.sf.taverna.t2.activities.wsdlsir;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
 
public class TestDialog extends JDialog {
  public JLabel InstructionsLabel1 = new JLabel();
  public JLabel userLabel = new JLabel();
  public JTextField userField = new JTextField();
  public JLabel passwordLabel = new JLabel();
  public JPasswordField passwordField = new JPasswordField();
  public JButton OkButton = new JButton();
  public JButton CancelButton = new JButton();
  
  private boolean canceled = false;

  /**
   * This class represents a dialog that prompts the user for a user and a password
   * @author Pablo Martin
   */
  public TestDialog(java.awt.Frame parent, boolean modal, String mensaje) {
    super(parent, modal);
 
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
    setTitle("Security configuration");
 
    setResizable(false);
 
    InstructionsLabel1.setText("<html>Insert user and password."
    		+ "<p>"
    		+ mensaje
    		+ "</p>"
    		+ "</html>");
 
    userLabel.setLabelFor(userField);
    userLabel.setText("User:");
 
 
    passwordLabel.setLabelFor(passwordField);
    passwordLabel.setText("Password:");
 
    OkButton.setText("Ok");
    OkButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae){
            try {
				TestDialog.this.setVisible(false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
        }
    });
    
    CancelButton.setText("Cancel");
    CancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae){
            try {
            	canceled=true;
				TestDialog.this.setVisible(false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
        }
    });
    GroupLayout layout = new GroupLayout(getContentPane());
 
    getContentPane().setLayout(layout);
 
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
 
    layout.setHorizontalGroup(
      layout.createParallelGroup()
      .addComponent(InstructionsLabel1, 450, 450, 450)
      .addGroup(
        layout.createSequentialGroup()
        .addGroup(
          layout.createParallelGroup()
          .addComponent(userLabel)
          .addComponent(passwordLabel)
        )
        .addGroup(
          layout.createParallelGroup()
          .addComponent(userField)
          .addComponent(passwordField)
          .addGroup(
            layout.createSequentialGroup()
            .addComponent(OkButton)
            .addComponent(CancelButton)
          )
        )
      )
    );
 
    layout.setVerticalGroup(
      layout.createSequentialGroup()
      .addComponent(InstructionsLabel1)
      .addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(userLabel)
        .addComponent(userField)
      )
      .addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(passwordLabel)
        .addComponent(passwordField)
      )
      .addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(OkButton)
        .addComponent(CancelButton)
      )
    );
 
    pack();
 
    setLocationRelativeTo(null);
  }

public boolean isCanceled() {
	return canceled;
}
}