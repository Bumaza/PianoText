package keystrokesimulator;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager.LookAndFeelInfo;

public class OptionsFrame extends JFrame {
   private JButton cancelButton;
   private JLabel jLabel1;
   private JLabel jLabel2;
   private JLabel jLabel3;
   private JButton saveButton;
   private JTextField thresholdField;
   private JLabel warning;

   public OptionsFrame() {
      this.initComponents();
      this.warning.setVisible(false);
      this.thresholdField.setText(KeystrokeSimulator.CHORD_THRESHOLD + "");
   }

   private void initComponents() {
      this.jLabel1 = new JLabel();
      this.jLabel2 = new JLabel();
      this.thresholdField = new JTextField();
      this.jLabel3 = new JLabel();
      this.saveButton = new JButton();
      this.cancelButton = new JButton();
      this.warning = new JLabel();
      this.setDefaultCloseOperation(2);
      this.jLabel1.setFont(new Font("Dialog", 1, 24));
      this.jLabel1.setText("Options");
      this.jLabel2.setText("Max. interkey time for chord recognition:");
      this.thresholdField.setHorizontalAlignment(4);
      this.thresholdField.setText("25");
      this.jLabel3.setText("ms");
      this.saveButton.setText("Save");
      this.saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            OptionsFrame.this.saveButtonActionPerformed(evt);
         }
      });
      this.cancelButton.setText("Cancel");
      this.cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            OptionsFrame.this.cancelButtonActionPerformed(evt);
         }
      });
      this.warning.setForeground(new Color(255, 51, 51));
      this.warning.setText("enter a valid value!");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(138, 138, 138).addComponent(this.warning)).addGroup(layout.createSequentialGroup().addComponent(this.jLabel2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.thresholdField, -2, 60, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jLabel3)).addComponent(this.jLabel1).addGroup(layout.createSequentialGroup().addComponent(this.saveButton).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.cancelButton))).addContainerGap(-1, 32767)));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jLabel1).addGap(54, 54, 54).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.jLabel2, -2, 36, -2).addComponent(this.thresholdField, -2, -1, -2).addComponent(this.jLabel3)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.warning).addPreferredGap(ComponentPlacement.RELATED, 63, 32767).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.saveButton).addComponent(this.cancelButton)).addContainerGap()));
      this.pack();
   }

   private void saveButtonActionPerformed(ActionEvent evt) {
      try {
         int thresh = Integer.parseInt(this.thresholdField.getText());
         KeystrokeSimulator.CHORD_THRESHOLD = thresh;

         try {
            FileWriter fs = new FileWriter(KeystrokeSimulator.OPTIONS_PATH);
            BufferedWriter out = new BufferedWriter(fs);
            out.write("CHORD_THRESHOLD=" + thresh);
            out.close();
         } catch (IOException var5) {
            Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, (String)null, var5);
         }

         this.setVisible(false);
         System.out.println(KeystrokeSimulator.CHORD_THRESHOLD);
      } catch (NumberFormatException var6) {
         this.warning.setVisible(true);
      }

   }

   private void cancelButtonActionPerformed(ActionEvent evt) {
      this.setVisible(false);
   }

   public static void main(String[] args) {
      try {
         LookAndFeelInfo[] arr$ = UIManager.getInstalledLookAndFeels();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            LookAndFeelInfo info = arr$[i$];
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException var5) {
         Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, (String)null, var5);
      } catch (InstantiationException var6) {
         Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, (String)null, var6);
      } catch (IllegalAccessException var7) {
         Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, (String)null, var7);
      } catch (UnsupportedLookAndFeelException var8) {
         Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, (String)null, var8);
      }

      EventQueue.invokeLater(new Runnable() {
         public void run() {
            (new OptionsFrame()).setVisible(true);
         }
      });
   }
}
