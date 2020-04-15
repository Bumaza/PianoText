package keystrokesimulator;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class wpmDisplay extends JFrame {
   private JLabel jLabel1;
   private JLabel wpmLabel;

   public wpmDisplay() {
      this.initComponents();
   }

   private void initComponents() {
      this.jLabel1 = new JLabel();
      this.wpmLabel = new JLabel();
      this.setAlwaysOnTop(true);
      this.addWindowListener(new WindowAdapter() {
         public void windowClosed(WindowEvent evt) {
            wpmDisplay.this.formWindowClosed(evt);
         }
      });
      this.jLabel1.setFont(new Font("Dialog", 1, 14));
      this.jLabel1.setText("Speed:");
      this.wpmLabel.setFont(new Font("Dialog", 1, 18));
      this.wpmLabel.setText("wpm");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jLabel1).addComponent(this.wpmLabel)).addContainerGap(87, 32767)));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jLabel1).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.wpmLabel).addContainerGap(-1, 32767)));
      this.pack();
   }

   private void formWindowClosed(WindowEvent evt) {
   }

   public void setWPM(String s) {
      this.wpmLabel.setText(s);
   }
}
