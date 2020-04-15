package PianoCommunication;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

public class PianoDevice {
   static String PIANO_LINUX = "MIDILINKmini";
   static String PIANO_WINDOWS = "MIDILINK-mini";

   public static void listTransmitterDevices() throws MidiUnavailableException {
      Info[] infos = MidiSystem.getMidiDeviceInfo();

      for(int i = 0; i < infos.length; ++i) {
         MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
         if (device.getMaxTransmitters() != 0) {
            System.out.println(device.getDeviceInfo().getName().toString() + " has transmitters");
         }
      }

   }

   public static Info[] getTransmitterDevicesList() throws MidiUnavailableException {
      Info[] infos = MidiSystem.getMidiDeviceInfo();
      return infos;
   }

   public static MidiDevice getInputDevice() throws MidiUnavailableException {
      Info[] infos = MidiSystem.getMidiDeviceInfo();

      for(int i = 0; i < infos.length; ++i) {
         MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
         System.out.println(device.getDeviceInfo().getName());
         if (device.getMaxTransmitters() != 0 && device.getDeviceInfo().getName().contains("MIDI")) {
            System.out.println(device.getDeviceInfo().getName().toString() + " was chosen");
            return device;
         }
      }

      return null;
   }
}
