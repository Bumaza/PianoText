package PianoCommunication;

import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import keystrokesimulator.Simulator;

public class DumpReceiver implements Receiver {
   public static long seByteCount = 0L;
   public static long smByteCount = 0L;
   public static long seCount = 0L;
   public static long smCount = 0L;
   private static final String[] sm_astrKeyNames = new String[]{"C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "Ais", "B"};
   private static final String[] sm_astrKeySignatures = new String[]{"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
   private static final String[] SYSTEM_MESSAGE_TEXT = new String[]{"System Exclusive (should not be in ShortMessage!)", "MTC Quarter Frame: ", "Song Position: ", "Song Select: ", "Undefined", "Undefined", "Tune Request", "End of SysEx (should not be in ShortMessage!)", "Timing clock", "Undefined", "Start", "Continue", "Stop", "Undefined", "Active Sensing", "System Reset"};
   private static final String[] QUARTER_FRAME_MESSAGE_TEXT = new String[]{"frame count LS: ", "frame count MS: ", "seconds count LS: ", "seconds count MS: ", "minutes count LS: ", "minutes count MS: ", "hours count LS: ", "hours count MS: "};
   private static final String[] FRAME_TYPE_TEXT = new String[]{"24 frames/second", "25 frames/second", "30 frames/second (drop)", "30 frames/second (non-drop)"};
   private PrintStream m_printStream;
   private boolean m_bDebug;
   private boolean m_bPrintTimeStampAsTicks;
   private static char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   public DumpReceiver(PrintStream printStream) {
      this(printStream, false);
   }

   public DumpReceiver(PrintStream printStream, boolean bPrintTimeStampAsTicks) {
      this.m_printStream = printStream;
      this.m_bDebug = false;
      this.m_bPrintTimeStampAsTicks = bPrintTimeStampAsTicks;
   }

   public void close() {
   }

   public void send(MidiMessage message, long time) {
      long lTimeStamp = (new Date()).getTime();
      String strMessage = null;
      if (message instanceof ShortMessage) {
         try {
            strMessage = this.decodeMessage((ShortMessage)message, lTimeStamp);
         } catch (Exception var10) {
            Logger.getLogger(DumpReceiver.class.getName()).log(Level.SEVERE, (String)null, var10);
         }
      } else if (message instanceof SysexMessage) {
         strMessage = this.decodeMessage((SysexMessage)message);
      } else if (message instanceof MetaMessage) {
         strMessage = this.decodeMessage((MetaMessage)message);
      } else {
         strMessage = "unknown message type";
      }

      String strTimeStamp = null;
      if (this.m_bPrintTimeStampAsTicks) {
         strTimeStamp = "tick " + lTimeStamp + ": ";
      } else if (lTimeStamp == -1L) {
         strTimeStamp = "timestamp [unknown]: ";
      } else {
         strTimeStamp = "timestamp " + lTimeStamp + " us: ";
      }

      if (strMessage != null) {
         if (message instanceof ShortMessage) {
            ShortMessage bla = (ShortMessage)message;
            String onOff = "";
            switch(bla.getCommand()) {
            case 128:
               onOff = "note Off ";
               break;
            case 144:
               if (bla.getData2() <= 0) {
                  onOff = "note Off ";
               } else {
                  onOff = "note On  ";
               }
            }
         }

         this.m_printStream.println(strTimeStamp + strMessage);
      }

   }

   public String decodeMessage(ShortMessage message, long timestamp) throws Exception {
      String strMessage;
      int nQType;
      strMessage = null;
      String[] handleMessage = null;
      label45:
      switch(message.getCommand()) {
      case 128:
         strMessage = "note Off " + getKeyName(message.getData1()) + " velocity: " + message.getData2();
         handleMessage = new String[]{"note Off", "" + message.getData1(), "" + message.getData2()};
         Simulator.handlePianoInput(timestamp, handleMessage);
         break;
      case 144:
         if (message.getData2() > 0) {
            strMessage = "note On " + getKeyName(message.getData1()) + " velocity: " + message.getData2();
            handleMessage = new String[]{"note On", "" + message.getData1(), "" + message.getData2()};
            Simulator.handlePianoInput(timestamp, handleMessage);
         } else {
            strMessage = "note Off " + getKeyName(message.getData1()) + " velocity: " + message.getData2();
            handleMessage = new String[]{"note Off", "" + message.getData1(), "" + message.getData2()};
            Simulator.handlePianoInput(timestamp, handleMessage);
         }
         break;
      case 160:
         strMessage = "polyphonic key pressure " + getKeyName(message.getData1()) + " pressure: " + message.getData2();
         break;
      case 176:
         if (message.getData2() == 127 && message.getData1() == 64) {
            strMessage = "control change " + message.getData1() + " value: " + message.getData2();
            handleMessage = new String[]{"control change", "" + message.getData2()};
            Simulator.handlePianoInput(timestamp, handleMessage);
         }
         break;
      case 192:
         strMessage = "program change " + message.getData1();
         break;
      case 208:
         strMessage = "key pressure " + getKeyName(message.getData1()) + " pressure: " + message.getData2();
      case 224:
         break;
      case 240:
         strMessage = SYSTEM_MESSAGE_TEXT[message.getChannel()];
         switch(message.getChannel()) {
         case 1:
            nQType = (message.getData1() & 112) >> 4;
            int nQData = message.getData1() & 15;
            if (nQType == 7) {
               nQData &= 1;
            }

            strMessage = strMessage + QUARTER_FRAME_MESSAGE_TEXT[nQType] + nQData;
            if (nQType == 7) {
               int nFrameType = (message.getData1() & 6) >> 1;
               strMessage = strMessage + ", frame type: " + FRAME_TYPE_TEXT[nFrameType];
            }
            break label45;
         case 2:
            strMessage = strMessage + get14bitValue(message.getData1(), message.getData2());
            break label45;
         case 3:
            strMessage = strMessage + message.getData1();
         default:
            break label45;
         }
      default:
         strMessage = "unknown message: status = " + message.getStatus() + ", byte1 = " + message.getData1() + ", byte2 = " + message.getData2();
      }

      if (message.getCommand() != 240) {
         nQType = message.getChannel() + 1;
         String strChannel = "channel " + nQType + ": ";
         if (strMessage != null) {
            strMessage = strChannel + strMessage;
         }
      }

      ++smCount;
      smByteCount += (long)message.getLength();
      return strMessage != null ? "[" + getHexString(message) + "] " + strMessage : strMessage;
   }

   public String decodeMessage(SysexMessage message) {
      byte[] abData = message.getData();
      String strMessage = null;
      if (message.getStatus() == 240) {
         strMessage = "Sysex message: F0" + getHexString(abData);
      } else if (message.getStatus() == 247) {
         strMessage = "Continued Sysex message F7" + getHexString(abData);
         --seByteCount;
      }

      seByteCount += (long)(abData.length + 1);
      ++seCount;
      return strMessage;
   }

   public String decodeMessage(MetaMessage message) {
      byte[] abMessage = message.getMessage();
      byte[] abData = message.getData();
      int nDataLength = message.getLength();
      String strMessage = null;
      switch(message.getType()) {
      case 0:
         int nSequenceNumber = (abData[0] & 255) << 8 | abData[1] & 255;
         strMessage = "Sequence Number: " + nSequenceNumber;
         break;
      case 1:
         String strText = new String(abData);
         strMessage = "Text Event: " + strText;
         break;
      case 2:
         String strCopyrightText = new String(abData);
         strMessage = "Copyright Notice: " + strCopyrightText;
         break;
      case 3:
         String strTrackName = new String(abData);
         strMessage = "Sequence/Track Name: " + strTrackName;
         break;
      case 4:
         String strInstrumentName = new String(abData);
         strMessage = "Instrument Name: " + strInstrumentName;
         break;
      case 5:
         String strLyrics = new String(abData);
         strMessage = "Lyric: " + strLyrics;
         break;
      case 6:
         String strMarkerText = new String(abData);
         strMessage = "Marker: " + strMarkerText;
         break;
      case 7:
         String strCuePointText = new String(abData);
         strMessage = "Cue Point: " + strCuePointText;
         break;
      case 32:
         int nChannelPrefix = abData[0] & 255;
         strMessage = "MIDI Channel Prefix: " + nChannelPrefix;
         break;
      case 47:
         strMessage = "End of Track";
         break;
      case 81:
         int nTempo = (abData[0] & 255) << 16 | (abData[1] & 255) << 8 | abData[2] & 255;
         float bpm = convertTempo((float)nTempo);
         bpm = (float)Math.round(bpm * 100.0F) / 100.0F;
         strMessage = "Set Tempo: " + bpm + " bpm";
         break;
      case 84:
         strMessage = "SMTPE Offset: " + (abData[0] & 255) + ":" + (abData[1] & 255) + ":" + (abData[2] & 255) + "." + (abData[3] & 255) + "." + (abData[4] & 255);
         break;
      case 88:
         strMessage = "Time Signature: " + (abData[0] & 255) + "/" + (1 << (abData[1] & 255)) + ", MIDI clocks per metronome tick: " + (abData[2] & 255) + ", 1/32 per 24 MIDI clocks: " + (abData[3] & 255);
         break;
      case 89:
         String strGender = abData[1] == 1 ? "minor" : "major";
         strMessage = "Key Signature: " + sm_astrKeySignatures[abData[0] + 7] + " " + strGender;
         break;
      case 127:
         String strDataDump = getHexString(abData);
         strMessage = "Sequencer-Specific Meta event: " + strDataDump;
         break;
      default:
         String strUnknownDump = getHexString(abData);
         strMessage = "unknown Meta event: " + strUnknownDump;
      }

      return strMessage;
   }

   public static String getKeyName(int nKeyNumber) {
      if (nKeyNumber > 127) {
         return "illegal value";
      } else {
         int nNote = nKeyNumber % 12;
         int nOctave = nKeyNumber / 12;
         return sm_astrKeyNames[nNote] + (nOctave - 1);
      }
   }

   public static String getKeyNameWithoutOctave(int nKeyNumber) {
      if (nKeyNumber > 127) {
         return "illegal value";
      } else {
         int nNote = nKeyNumber % 12;
         return sm_astrKeyNames[nNote];
      }
   }

   public static int get14bitValue(int nLowerPart, int nHigherPart) {
      return nLowerPart & 127 | (nHigherPart & 127) << 7;
   }

   private static int signedByteToUnsigned(byte b) {
      return b & 255;
   }

   private static float convertTempo(float value) {
      if (value <= 0.0F) {
         value = 0.1F;
      }

      return 6.0E7F / value;
   }

   public static String getHexString(byte[] aByte) {
      StringBuffer sbuf = new StringBuffer(aByte.length * 3 + 2);

      for(int i = 0; i < aByte.length; ++i) {
         sbuf.append(' ');
         sbuf.append(hexDigits[(aByte[i] & 240) >> 4]);
         sbuf.append(hexDigits[aByte[i] & 15]);
      }

      return new String(sbuf);
   }

   private static String intToHex(int i) {
      return "" + hexDigits[(i & 240) >> 4] + hexDigits[i & 15];
   }

   public static String getHexString(ShortMessage sm) {
      int status = sm.getStatus();
      String res = intToHex(sm.getStatus());
      switch(status) {
      case 246:
      case 247:
      case 248:
      case 249:
      case 250:
      case 251:
      case 252:
      case 253:
      case 254:
      case 255:
         return res;
      default:
         res = res + ' ' + intToHex(sm.getData1());
         switch(status) {
         case 241:
         case 243:
            return res;
         default:
            switch(sm.getCommand()) {
            case 192:
            case 208:
               return res;
            default:
               res = res + ' ' + intToHex(sm.getData2());
               return res;
            }
         }
      }
   }
}
