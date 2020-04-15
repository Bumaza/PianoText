package keystrokesimulator;

import Mapping.Chord;
import Mapping.Letter;
import Mapping.Mapping;
import Mapping.NGram;
import Mapping.Note;
import PianoCommunication.DumpReceiver;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

public class Simulator {
   private static boolean soundOn = true;
   private static Robot robot;
   private static Synthesizer synth;
   private static Receiver synthRcvr;
   private static int chord_threshold;
   private static int enter_threshold = 350;
   private static Mapping mapping;
   private static ArrayList<String[]> notesPressedDown = new ArrayList();
   private static ArrayList<String[]> remove = new ArrayList();
   private static long lastPedalPress;
   private static String textTillEnter = "";
   private static long lastTime;
   private static long timeOverall = 0L;
   private static long sentence_threshold = 4000L;

   public static void init(Mapping mapping, int threshold) {
      chord_threshold = threshold;

      try {
         robot = new Robot();
      } catch (AWTException var4) {
         Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, (String)null, var4);
      }

      Simulator.mapping = mapping;

      try {
         synth = MidiSystem.getSynthesizer();
         synth.open();
         synthRcvr = synth.getReceiver();
      } catch (Exception var3) {
         System.out.println(var3);
      }

   }

   public static void handlePianoInput(long lTimeStamp, String[] msg) throws Exception {
      String var3 = msg[0];
      byte var4 = -1;
      switch(var3.hashCode()) {
      case -1165442093:
         if (var3.equals("control change")) {
            var4 = 2;
         }
         break;
      case 1579558945:
         if (var3.equals("note Off")) {
            var4 = 0;
         }
         break;
      case 2129163501:
         if (var3.equals("note On")) {
            var4 = 1;
         }
      }

      String[] m;
      switch(var4) {
      case 0:
         soundSynth(msg);
         if (contains(notesPressedDown, msg[1])) {
            switch(notesPressedDown.size()) {
            case 0:
               break;
            case 1:
               label61: {
                  m = (String[])notesPressedDown.get(0);
                  if (m[1] == null) {
                     if (msg[1] != null) {
                        break label61;
                     }
                  } else if (!m[1].equals(msg[1])) {
                     break label61;
                  }

                  int midi = Integer.parseInt(m[1]);
                  processSingle(midi, lTimeStamp);
               }

               notesPressedDown = new ArrayList();
               break;
            default:
               if (notesPressedDown.size() > 10) {
                  throw new Exception("something wrong with chord recognition, too many notes in notesPressedDown variable");
               }

               long pushTimestamp = 0L;

               for(int i = 0; i < notesPressedDown.size(); ++i) {
                  String[] notemsg = (String[])notesPressedDown.get(i);
                  if (notemsg[1].equals(msg[1])) {
                     pushTimestamp = Long.parseLong(notemsg[0]);
                     break;
                  }
               }

               Integer[] notes = getNotesPressedSameTimestamp(pushTimestamp, chord_threshold);
               switch(notes.length) {
               case 1:
                  processSingle(notes[0], lTimeStamp);
                  break;
               default:
                  processChord(notes, lTimeStamp, msg);
               }
            }
         }
         break;
      case 1:
         soundSynth(msg);
         m = new String[]{lTimeStamp + "", msg[1]};
         notesPressedDown.add(m);
         break;
      case 2:
         if (!msg[1].equals("0")) {
            if (lTimeStamp - lastPedalPress < (long)enter_threshold) {
               simulateKeystroke("enter", lTimeStamp);
               lastPedalPress = 0L;
            } else {
               simulateKeystroke(" ", lTimeStamp);
               lastPedalPress = lTimeStamp;
            }
         }
      }

   }

   private static Integer[] getNotesPressedSameTimestamp(long timestamp, int plusminus) {
      ArrayList<Integer> notes = new ArrayList();
      long left = timestamp - (long)plusminus;
      long right = timestamp + (long)plusminus;
      remove = new ArrayList();

      String[] s;
      for(int i = 0; i < notesPressedDown.size(); ++i) {
         s = (String[])notesPressedDown.get(i);
         long ts = Long.parseLong(s[0]);
         if (left <= ts && ts <= right) {
            notes.add(Integer.parseInt(s[1]));
            remove.add(s);
         }
      }

      Iterator i$ = remove.iterator();

      while(i$.hasNext()) {
         s = (String[])i$.next();
         notesPressedDown.remove(s);
      }

      Integer[] res = new Integer[notes.size()];
      notes.toArray(res);
      return res;
   }

   public static void soundSynth(String[] m) {
      if (soundOn) {
         ShortMessage myMsg = new ShortMessage();

         try {
            if ("note On".equals(m[0])) {
               myMsg.setMessage(144, 4, Integer.parseInt(m[1]), Integer.parseInt(m[2]) + 50);
            } else {
               myMsg.setMessage(128, 4, Integer.parseInt(m[1]), 0);
            }

            synthRcvr.send(myMsg, -1L);
         } catch (InvalidMidiDataException | NumberFormatException var3) {
            System.out.println(var3);
         }
      }

   }

   public static void processSingle(int midi, long lTimeStamp) {
      try {
         String nKey = DumpReceiver.getKeyName(midi).toUpperCase();
         Note note = (Note)mapping.notes.get(nKey);
         Letter letter = note.mappedTo;
         Integer[] var10000 = new Integer[]{note.midiNumber};
         System.out.println("Note: " + note.name + note.octave + "letter: " + letter.name);
         simulateKeystroke(letter.toString(), lTimeStamp);
      } catch (Exception var7) {
      }

   }

   public static void processChord(Integer[] notes, Long lTimeStamp, String[] msg) {
      Arrays.sort(notes);
      String cKey = "[" + DumpReceiver.getKeyNameWithoutOctave(notes[0]).toUpperCase();

      for(int i = 1; i < notes.length; ++i) {
         cKey = cKey + ", " + DumpReceiver.getKeyNameWithoutOctave(notes[i]).toUpperCase();
      }

      cKey = cKey + "]";

      try {
         Chord chord = (Chord)mapping.chords.get(cKey);
         NGram ngram = chord.mappedTo;
         System.out.println("Chord: " + cKey + "string: " + ngram.toString());
         Integer[] arr$ = notes;
         int len$ = notes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int n = arr$[i$];
            String[] myMsg = new String[]{"note Off", n + ""};
            soundSynth(myMsg);
         }

         simulateKeystroke(ngram.toString(), lTimeStamp);
      } catch (Exception var11) {
         int note = Integer.parseInt(msg[1]);
         Iterator i$ = remove.iterator();

         while(i$.hasNext()) {
            String[] s = (String[])i$.next();
            if (!s[1].equals(msg[1])) {
               notesPressedDown.add(s);
            }
         }

         processSingle(note, lTimeStamp);
      }

   }

   private static boolean contains(ArrayList<String[]> notesPressedDown, String string) {
      Iterator i$ = notesPressedDown.iterator();

      String[] msg;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         msg = (String[])i$.next();
      } while(!string.equals(msg[1]));

      return true;
   }

   private static void simulateKeystroke(String text, long timestamp) {
      if (text.equals("enter")) {
         textTillEnter = "";
         timeOverall = 0L;
         lastTime = 0L;
         robot.keyPress(10);
         robot.keyRelease(10);
         System.out.println("restarted wpm");
      } else if(text.equals("BACKSPACE")){
         timeOverall = 0L;
         lastTime = 0L;
         robot.keyPress(8);
         robot.keyRelease(8);
         System.out.println("restarted wpm");
      } else if(text.equals("ENTER")){
         timeOverall = 0L;
         lastTime = 0L;
         robot.keyPress(10);
         robot.keyRelease(10);
         System.out.println("restarted wpm");
      } else {
         long diff = timestamp - lastTime;
         if (diff > sentence_threshold) {
            textTillEnter = text;
            timeOverall = 0L;
            lastTime = timestamp;
         } else {
            textTillEnter = textTillEnter + text;
            if (lastTime == 0L) {
               lastTime = timestamp;
            } else {
               timeOverall += diff;
               lastTime = timestamp;
               if (KeystrokeSimulator.showWPM) {
                  updateWPM();
               }
            }
         }

         for(int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (text.equals(" ")) {
               keyCode = 32;
            }

            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
         }
      }

   }

   private static void updateWPM() {
      int numberOfLetters = textTillEnter.length() - 1;
      double cpms = (double)numberOfLetters / (double)timeOverall;
      double wpm = cpms * 1000.0D * 60.0D / 5.0D;
      double round = (double)Math.round(wpm * 100.0D) / 100.0D;
      KeystrokeSimulator.wpm.setWPM(round + " wpm");
   }
}
