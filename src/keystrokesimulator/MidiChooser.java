package keystrokesimulator;

import PianoCommunication.PianoDevice;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class MidiChooser extends JFrame {
   Info[] infos;
   private JButton cancelButton;
   private JLabel descriptionLabel;
   private JComboBox deviceList;
   private JButton okButton;

   public MidiChooser() {
      this.initComponents();

      try {
         this.infos = PianoDevice.getTransmitterDevicesList();
         String[] infoList = new String[this.infos.length];

         for(int i = 0; i < this.infos.length; ++i) {
            MidiDevice device = MidiSystem.getMidiDevice(this.infos[i]);
            if (device.getMaxTransmitters() != 0) {
               infoList[i] = device.getDeviceInfo().getName().toString();
            }
         }

         this.deviceList.setModel(new DefaultComboBoxModel(infoList));
      } catch (MidiUnavailableException var4) {
         this.descriptionLabel.setText("NO MIDI DEVICE AVAILABLE");
         this.okButton.setEnabled(false);
         this.deviceList.setEnabled(false);
         this.deviceList.setModel(new DefaultComboBoxModel(new String[0]));
      }

   }

   private void initComponents() {
      this.deviceList = new JComboBox();
      this.okButton = new JButton();
      this.cancelButton = new JButton();
      this.descriptionLabel = new JLabel();
      this.setDefaultCloseOperation(3);
      this.deviceList.setMaximumRowCount(20);
      this.deviceList.setModel(new DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
      this.okButton.setText("OK");
      this.okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MidiChooser.this.okButtonActionPerformed(evt);
         }
      });
      this.cancelButton.setText("Cancel");
      this.cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MidiChooser.this.cancelButtonActionPerformed(evt);
         }
      });
      this.descriptionLabel.setFont(new Font("Dialog", 1, 18));
      this.descriptionLabel.setText(" Choose the Piano Midi Device");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.descriptionLabel)).addGroup(layout.createSequentialGroup().addGap(76, 76, 76).addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(this.cancelButton).addComponent(this.deviceList, -2, 196, -2)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.okButton))).addContainerGap(-1, 32767)));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(21, 21, 21).addComponent(this.descriptionLabel).addGap(29, 29, 29).addComponent(this.deviceList, -2, -1, -2).addGap(31, 31, 31).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.cancelButton).addComponent(this.okButton)).addContainerGap(-1, 32767)));
      this.pack();
   }

   private void cancelButtonActionPerformed(ActionEvent evt) {
      this.setVisible(false);
   }

   private void okButtonActionPerformed(ActionEvent evt) {
      this.setVisible(false);
      int i = this.deviceList.getSelectedIndex();
      if (KeystrokeSimulator.piano != null) {
         KeystrokeSimulator.closePiano();
      }

      try {
         MidiDevice device = MidiSystem.getMidiDevice(this.infos[i]);
         KeystrokeSimulator.piano = device;
      } catch (Exception var4) {
         Logger.getLogger(MidiChooser.class.getName()).log(Level.SEVERE, (String)null, var4);
      }

   }
}
