package net.sf.taverna.t2.activities.wsdlsir;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
 
public class TestDialog extends JDialog {
  public JLabel InstructionsLabel1 = new JLabel();
  public JLabel userLabel = new JLabel();
  public JTextField userField = new JTextField();
//  public JButton SendRecoveryCode = new JButton();
//  public JLabel InstructionsLabel2 = new JLabel();
//  public JLabel RecoveryCodeLabel = new JLabel();
//  public JTextField RecoveryCode = new JTextField();
  public JLabel passwordLabel = new JLabel();
  public JPasswordField passwordField = new JPasswordField();
//  public JLabel confirmPasswordLabel = new JLabel();
//  public JPasswordField confirmPasswordField = new JPasswordField();
  public JButton OkButton = new JButton();
  public JButton CancelButton = new JButton();
  
  private boolean canceled = false;
 
  public TestDialog(java.awt.Frame parent, boolean modal, String mensaje) {
    super(parent, modal);
 
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
    setTitle("Security configuration");
 
    setResizable(false);
 
    InstructionsLabel1.setText("<html>Introduce usuario y contraseña."
    		+ "<p>"
    		+ mensaje
    		+ "</p>"
    		+ "</html>");
 
    userLabel.setLabelFor(userField);
    userLabel.setText("User:");
 
//    SendRecoveryCode.setText("Send Recovery Code");
 
//    InstructionsLabel2.setText("<html>When you have received your recovery code, enter it into the space provided, enter your new password twice and then click <strong>Change Password</strong>.</html>");
// 
//    RecoveryCodeLabel.setLabelFor(RecoveryCode);
//    RecoveryCodeLabel.setText("Recovery Code:");
 
    passwordLabel.setLabelFor(passwordField);
    passwordLabel.setText("Password:");
 
//    confirmPasswordLabel.setLabelFor(confirmPasswordField);
//    confirmPasswordLabel.setText("Confirm Password:");
 
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
//      .addComponent(InstructionsLabel2, 450, 450, 450)
      .addGroup(
        layout.createSequentialGroup()
        .addGroup(
          layout.createParallelGroup()
          .addComponent(userLabel)
//          .addComponent(RecoveryCodeLabel)
          .addComponent(passwordLabel)
//          .addComponent(confirmPasswordLabel)
        )
        .addGroup(
          layout.createParallelGroup()
          .addComponent(userField)
//          .addComponent(RecoveryCode)
          .addComponent(passwordField)
//          .addComponent(confirmPasswordField)
//          .addComponent(SendRecoveryCode)
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
//      .addComponent(SendRecoveryCode)
//      .addComponent(InstructionsLabel2)
//      .addGroup(
//        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//        .addComponent(RecoveryCodeLabel)
//        .addComponent(RecoveryCode)
//      )
      .addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(passwordLabel)
        .addComponent(passwordField)
      )
//      .addGroup(
//        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//        .addComponent(confirmPasswordLabel)
//        .addComponent(confirmPasswordField)
//      )
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