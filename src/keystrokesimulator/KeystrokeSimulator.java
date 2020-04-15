package keystrokesimulator;

import Mapping.Mapping;
import PianoCommunication.DumpReceiver;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.jdom2.JDOMException;

public class KeystrokeSimulator {
   private static String MAPPING_PATH = "mapping.xml";
   public static Mapping mapping;
   public static MidiDevice piano;
   public static wpmDisplay wpm;
   public static int CHORD_THRESHOLD = 25;
   public static boolean TESTING = false;
   public static String OPTIONS_PATH = "src/keystrokesimulator/options.txt";
   public static boolean showWPM = false;

   public static void main(String[] args) throws AWTException, MidiUnavailableException, JDOMException, IOException, Exception {
      try {
         UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      } catch (Exception var7) {
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
         } catch (Exception var6) {
         }
      }

      UIManager.put("swing.boldMetal", Boolean.FALSE);
      initOptions();
      if (SystemTray.isSupported()) {
         initTray();
      } else {
         System.err.println("System tray not supported");
      }

      mapping = new Mapping(MAPPING_PATH);
      Simulator.init(mapping, CHORD_THRESHOLD);
      EventQueue.invokeLater(new Runnable() {
         public void run() {
            (new MidiChooser()).setVisible(true);
         }
      });
      makePianoReady();
   }

   public static void makePianoReady() throws IOException, InterruptedException {
      while(piano == null) {
         Thread.sleep(1000L);
      }

      if (showWPM) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               KeystrokeSimulator.wpm = new wpmDisplay();
               KeystrokeSimulator.wpm.setVisible(true);
            }
         });
      }

      try {
         if (!(piano instanceof KeyboardTestMidi)) {
            System.out.println("open inputDevice: " + piano.getDeviceInfo().toString());
            piano.open();
            System.out.println("connect Transmitter to Receiver, max Receiver");
         }
      } catch (NullPointerException | MidiUnavailableException var8) {
         System.err.println(var8);
      }

      try {
         Transmitter transmitter = piano.getTransmitter();
         Receiver r = new DumpReceiver(System.out);
         transmitter.setReceiver(r);
      } catch (Exception var6) {
         Logger.getLogger(KeystrokeSimulator.class.getName()).log(Level.SEVERE, (String)null, var6);
      } finally {
         System.out.println("connected.");
         System.out.println("running...");
         System.in.read();
         closePiano();
      }

   }

   public static void closePiano() {
      System.out.println("close inputDevice: " + piano.getDeviceInfo().toString());
      piano.close();
      System.out.println("Received " + DumpReceiver.seCount + " sysex messages with a total of " + DumpReceiver.seByteCount + " bytes");
      System.out.println("Received " + DumpReceiver.smCount + " short messages with a total of " + DumpReceiver.smByteCount + " bytes");
      System.out.println("Received a total of " + (DumpReceiver.smByteCount + DumpReceiver.seByteCount) + " bytes");
   }

   public static void initOptions() {
      try {
         FileReader fr = new FileReader(OPTIONS_PATH);
         BufferedReader br = new BufferedReader(fr);

         String currentRecord;
         while((currentRecord = br.readLine()) != null && !currentRecord.equals("")) {
            String[] str = currentRecord.split("=");
            String var5 = str[0];
            byte var6 = -1;
            switch(var5.hashCode()) {
            case -1709316088:
               if (var5.equals("CHORD_THRESHOLD")) {
                  var6 = 0;
               }
               break;
            case 2067282039:
               if (var5.equals("showWPM")) {
                  var6 = 1;
               }
            }

            switch(var6) {
            case 0:
               CHORD_THRESHOLD = Integer.parseInt(str[1]);
               break;
            case 1:
               showWPM = Boolean.parseBoolean(str[1]);
            }
         }
      } catch (NumberFormatException | IOException var7) {
      }

   }

   private static void initTray() {
      URL imageURL = KeystrokeSimulator.class.getResource("/keystrokesimulator/icon.png");
      Image icon = (new ImageIcon(imageURL, "tray icon")).getImage();
      final TrayIcon trayIcon = new TrayIcon(icon);
      trayIcon.setImageAutoSize(true);
      final JPopupMenu popup = new JPopupMenu();
      final SystemTray tray = SystemTray.getSystemTray();
      JMenuItem aboutItem = new JMenuItem("About");
      JMenuItem optionsItem = new JMenuItem("Options");
      JMenuItem exitItem = new JMenuItem("Exit");
      JMenuItem chooseDeviceItem = new JMenuItem("Choose midi device");
      JCheckBoxMenuItem cb1 = new JCheckBoxMenuItem("Show wpm");
      popup.add(aboutItem);
      popup.addSeparator();
      popup.add(cb1);
      popup.add(optionsItem);
      popup.add(chooseDeviceItem);
      popup.addSeparator();
      popup.add(exitItem);
      trayIcon.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            if (!popup.isVisible()) {
               Rectangle bounds = KeystrokeSimulator.getSafeScreenBounds(e.getPoint());
               Point point = e.getPoint();
               int x = point.x;
               int y = point.y;
               if (y < bounds.y) {
                  y = bounds.y;
               } else if (y > bounds.y + bounds.height) {
                  y = bounds.y + bounds.height;
               }

               if (x < bounds.x) {
                  x = bounds.x;
               } else if (x > bounds.x + bounds.width) {
                  x = bounds.x + bounds.width;
               }

               if (x + popup.getPreferredSize().width > bounds.x + bounds.width) {
                  x = bounds.x + bounds.width - popup.getPreferredSize().width;
               }

               if (y + popup.getPreferredSize().height > bounds.y + bounds.height) {
                  y = bounds.y + bounds.height - popup.getPreferredSize().height;
               }

               popup.setLocation(x, y);
               popup.setVisible(true);
            } else {
               popup.setVisible(false);
            }

         }
      });
      cb1.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            int cb1Id = e.getStateChange();
            if (cb1Id == 1) {
               KeystrokeSimulator.wpm = new wpmDisplay();
               KeystrokeSimulator.wpm.setVisible(true);
               KeystrokeSimulator.showWPM = true;
            } else {
               KeystrokeSimulator.wpm.setVisible(false);
               KeystrokeSimulator.showWPM = true;
            }

            popup.setVisible(false);
         }
      });
      aboutItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog((Component)null, "http://www.pianoText.mpi-inf.mpg.de");
            popup.setVisible(false);
         }
      });
      optionsItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            (new OptionsFrame()).setVisible(true);
            popup.setVisible(false);
         }
      });
      exitItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            tray.remove(trayIcon);
            System.exit(0);
         }
      });
      chooseDeviceItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            (new MidiChooser()).setVisible(true);
            popup.setVisible(false);
         }
      });

      try {
         SystemTray.getSystemTray().add(trayIcon);
      } catch (AWTException var11) {
         Logger.getLogger(KeystrokeSimulator.class.getName()).log(Level.SEVERE, (String)null, var11);
      }

   }

   public static Rectangle getSafeScreenBounds(Point pos) {
      Rectangle bounds = getScreenBoundsAt(pos);
      Insets insets = getScreenInsetsAt(pos);
      bounds.x += insets.left;
      bounds.y += insets.top;
      bounds.width -= insets.left + insets.right;
      bounds.height -= insets.top + insets.bottom;
      return bounds;
   }

   public static Insets getScreenInsetsAt(Point pos) {
      GraphicsDevice gd = getGraphicsDeviceAt(pos);
      Insets insets = null;
      if (gd != null) {
         insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
      }

      return insets;
   }

   public static Rectangle getScreenBoundsAt(Point pos) {
      GraphicsDevice gd = getGraphicsDeviceAt(pos);
      Rectangle bounds = null;
      if (gd != null) {
         bounds = gd.getDefaultConfiguration().getBounds();
      }

      return bounds;
   }

   public static GraphicsDevice getGraphicsDeviceAt(Point pos) {
      GraphicsDevice device = null;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] lstGDs = ge.getScreenDevices();
      ArrayList<GraphicsDevice> lstDevices = new ArrayList(lstGDs.length);
      GraphicsDevice[] arr$ = lstGDs;
      int len$ = lstGDs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         GraphicsDevice gd = arr$[i$];
         GraphicsConfiguration gc = gd.getDefaultConfiguration();
         Rectangle screenBounds = gc.getBounds();
         if (screenBounds.contains(pos)) {
            lstDevices.add(gd);
         }
      }

      if (lstDevices.size() > 0) {
         device = (GraphicsDevice)lstDevices.get(0);
      } else {
         device = ge.getDefaultScreenDevice();
      }

      return device;
   }
}
