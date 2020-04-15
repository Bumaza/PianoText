package keystrokesimulator;

import Mapping.Letter;
import Mapping.Mapping;
import Mapping.Note;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;

public class KeyboardTestMidi implements MidiDevice {
   Mapping mapping;
   KeyboardTransmitter t;

   public KeyboardTestMidi(Mapping m) {
      this.mapping = m;
      this.t = new KeyboardTransmitter(this.mapping);
   }

   public Info getDeviceInfo() {
      return null;
   }

   public void open() throws MidiUnavailableException {
   }

   public void close() {
   }

   public boolean isOpen() {
      return true;
   }

   public long getMicrosecondPosition() {
      return 0L;
   }

   public int getMaxReceivers() {
      return 100;
   }

   public int getMaxTransmitters() {
      return 100;
   }

   public Receiver getReceiver() throws MidiUnavailableException {
      return this.t.receiver;
   }

   public List<Receiver> getReceivers() {
      return this.t.rcv;
   }

   public Transmitter getTransmitter() throws MidiUnavailableException {
      return this.t;
   }

   public KeyListener getKeyListener() {
      return this.t;
   }

   public List<Transmitter> getTransmitters() {
      return null;
   }

   public class KeyboardTransmitter implements Transmitter, KeyListener {
      Receiver receiver;
      ArrayList<Receiver> rcv;
      Mapping mapping;

      private KeyboardTransmitter(Mapping mapping) {
         this.receiver = null;
         this.rcv = new ArrayList();
         this.mapping = mapping;
      }

      public void setReceiver(Receiver rcvr) {
         this.receiver = rcvr;
         this.rcv.add(rcvr);
      }

      public Receiver getReceiver() {
         return this.receiver;
      }

      public void close() {
      }

      public void keyTyped(KeyEvent ke) {
      }

      public void keyPressed(KeyEvent ke) {
         Date date = new Date();
         long l = date.getTime();
         ShortMessage myMsg = new ShortMessage();
         String s = ke.getKeyChar() + "";
         if (s.equals(" ")) {
            try {
               myMsg.setMessage(176, 64, 127);
               this.receiver.send(myMsg, l);
            } catch (InvalidMidiDataException var12) {
               Logger.getLogger(KeyboardTestMidi.class.getName()).log(Level.SEVERE, (String)null, var12);
            }
         } else {
            Letter letter = (Letter)this.mapping.letters.get(s);

            try {
               Note note = (Note)letter.mappedTo.get(0);
               int midiNumber = Mapping.getNoteValue(note.name, note.octave);
               myMsg.setMessage(144, 4, midiNumber, 60);
               Iterator i$ = this.rcv.iterator();

               while(i$.hasNext()) {
                  Receiver r = (Receiver)i$.next();
                  r.send(myMsg, l);
               }
            } catch (Exception var13) {
            }
         }

      }

      public void keyReleased(KeyEvent ke) {
         Date date = new Date();
         long l = date.getTime();
         String s = ke.getKeyChar() + "";
         Letter letter = (Letter)this.mapping.letters.get(s);

         try {
            Note note = (Note)letter.mappedTo.get(0);
            int midiNumber = Mapping.getNoteValue(note.name, note.octave);
            ShortMessage myMsg = new ShortMessage();
            myMsg.setMessage(128, 4, midiNumber, 0);
            Iterator i$ = this.rcv.iterator();

            while(i$.hasNext()) {
               Receiver r = (Receiver)i$.next();
               r.send(myMsg, l);
            }
         } catch (Exception var12) {
         }

      }

      // $FF: synthetic method
      KeyboardTransmitter(Mapping x1, Object x2) {
         this(x1);
      }
   }
}
