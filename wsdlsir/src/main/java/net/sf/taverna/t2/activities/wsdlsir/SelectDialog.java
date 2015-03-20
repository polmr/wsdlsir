package net.sf.taverna.t2.activities.wsdlsir;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.*;
 
/**
 * This class represents a dialog that prompts the user for a selection within different options (Idps)
 * @author Pablo Martin
 *
 */
public class SelectDialog extends JDialog {
  public JLabel InstructionsLabel1 = new JLabel();
  public JLabel selectLabel = new JLabel();
  public JComboBox idpSelect;
  public JButton OkButton = new JButton();
  public JButton CancelButton = new JButton();
  
  
  private boolean canceled = false;
 
  public SelectDialog(java.awt.Frame parent, boolean modal, Map<String,String> options, String mensaje, String title) {
    super(parent, modal);
 
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
    setTitle("Security configuration");
 
    setResizable(false);
 
    InstructionsLabel1.setText("<html>Select  identity provider "
    		+ "for "
    		+ title
    		+ "."
    		+ "<p>"
    		+ mensaje
    		+ "</p>"
    		+ "</html>");
 
    
    
    idpSelect = new JComboBox(options.keySet().toArray());
    selectLabel.setLabelFor(idpSelect);
    selectLabel.setText("Identity Provider:");
    
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
      .addGroup(
        layout.createSequentialGroup()
        .addGroup(
          layout.createParallelGroup()
          .addComponent(selectLabel)
        )
        .addGroup(
          layout.createParallelGroup()
          .addComponent(idpSelect)
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