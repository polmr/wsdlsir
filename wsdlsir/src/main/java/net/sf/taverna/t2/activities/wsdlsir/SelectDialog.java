package net.sf.taverna.t2.activities.wsdlsir;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.*;
 
public class SelectDialog extends JDialog {
  public JLabel InstructionsLabel1 = new JLabel();
  public JLabel selectLabel = new JLabel();
  public JComboBox idpSelect;// = new JComboBox();
//  public JButton SendRecoveryCode = new JButton();
//  public JLabel InstructionsLabel2 = new JLabel();
//  public JLabel RecoveryCodeLabel = new JLabel();
//  public JTextField RecoveryCode = new JTextField();
//  public JLabel confirmPasswordLabel = new JLabel();
//  public JPasswordField confirmPasswordField = new JPasswordField();
  public JButton OkButton = new JButton();
  public JButton CancelButton = new JButton();
  
  
  private boolean canceled = false;
 
  public SelectDialog(java.awt.Frame parent, boolean modal, Map<String,String> opciones, String mensaje, String titulo) {
    super(parent, modal);
 
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
    setTitle("Security configuration");
 
    setResizable(false);
 
    InstructionsLabel1.setText("<html>Seleccione proveedor de identidad "
    		+ "para "
    		+ titulo
    		+ "."
    		+ "<p>"
    		+ mensaje
    		+ "</p>"
    		+ "</html>");
 
    
    
    idpSelect = new JComboBox(opciones.keySet().toArray());
//    		studentGrades.keySet()
    selectLabel.setLabelFor(idpSelect);
    selectLabel.setText("Identity Provider:");
    
    
 
//    SendRecoveryCode.setText("Send Recovery Code");
 
//    InstructionsLabel2.setText("<html>When you have received your recovery code, enter it into the space provided, enter your new password twice and then click <strong>Change Password</strong>.</html>");
// 
//    RecoveryCodeLabel.setLabelFor(RecoveryCode);
//    RecoveryCodeLabel.setText("Recovery Code:");
 
//    confirmPasswordLabel.setLabelFor(confirmPasswordField);
//    confirmPasswordLabel.setText("Confirm Password:");
 
    OkButton.setText("Ok");
    OkButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae){
            try {
				SelectDialog.this.setVisible(false);
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
            	SelectDialog.this.setVisible(false);
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
          .addComponent(selectLabel)
//          .addComponent(RecoveryCodeLabel)
//          .addComponent(passwordLabel)
//          .addComponent(confirmPasswordLabel)
        )
        .addGroup(
          layout.createParallelGroup()
          .addComponent(idpSelect)
//          .addComponent(RecoveryCode)
//          .addComponent(passwordField)
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
        .addComponent(selectLabel)
        .addComponent(idpSelect)
      )
//      .addComponent(SendRecoveryCode)
//      .addComponent(InstructionsLabel2)
//      .addGroup(
//        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//        .addComponent(RecoveryCodeLabel)
//        .addComponent(RecoveryCode)
//      )
//      .addGroup(
//        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//        .addComponent(passwordLabel)
//        .addComponent(passwordField)
//      )
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
    setSize(getWidth(),getHeight()+15);
    
    setLocationRelativeTo(null);
  }

public boolean isCanceled() {
	return canceled;
}
}