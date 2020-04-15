package Mapping;

import java.util.Arrays;
import java.util.List;

public class Chord {
   public String[] notes;
   public NGram mappedTo;
   private static List<String> chromas = Arrays.asList("C", "CIS", "D", "DIS", "E", "F", "FIS", "G", "GIS", "A", "AIS", "B");

   public Chord(String[] notes, NGram mapped) {
      this.notes = notes;
      this.mappedTo = mapped;
   }

   public String toString() {
      String s = "[" + this.notes[0];

      for(int i = 1; i < this.notes.length; ++i) {
         s = s + ", " + this.notes[i];
      }

      s = s + "]";
      return s;
   }

   public ChordRealization getChordRealizationInMiddleOf(Hand hand) {
      int curr;
      switch(hand) {
      case LEFT:
         String last = this.notes[this.notes.length - 1];
         int lastchr = chromas.indexOf(last);
         int octave = 3;
         if (lastchr <= 4) {
            octave = 4;
         }

         int[] midiNotes = new int[this.notes.length];
         midiNotes[midiNotes.length - 1] = Mapping.getNoteValue(last, octave);

         for(int i = this.notes.length - 2; i >= 0; --i) {
            curr = chromas.indexOf(this.notes[i]);
            if (curr >= chromas.indexOf(this.notes[i + 1])) {
               --octave;
            }

            midiNotes[i] = Mapping.getNoteValue(this.notes[i], octave);
         }

         return new ChordRealization(this, midiNotes);
      case RIGHT:
         String first = this.notes[0];
         curr = chromas.indexOf(first);
         int oct = 4;
         if (curr >= 10) {
            boolean var4 = true;
         }

         int[] midiNotes2 = new int[this.notes.length];
         midiNotes2[0] = Mapping.getNoteValue(first, oct);

         for(int i = 1; i < this.notes.length; ++i) {
            curr = chromas.indexOf(this.notes[i]);
            if (curr <= chromas.indexOf(this.notes[i - 1])) {
               ++oct;
            }

            midiNotes2[i] = Mapping.getNoteValue(this.notes[i], oct);
         }

         return new ChordRealization(this, midiNotes2);
      default:
         return null;
      }
   }

   public class ChordRealization {
      public Chord chord;
      public int[] midiNotes;
      public Hand hand;

      public ChordRealization(Chord chord, int[] midiNotes) {
         this.chord = chord;
         this.midiNotes = midiNotes;
         if ((double)this.getHighestNote() >= Hand.LEFT.upperBound()) {
            this.hand = Hand.RIGHT;
         } else if ((double)this.getLowestNote() <= Hand.RIGHT.lowerBound()) {
            this.hand = Hand.LEFT;
         } else {
            this.hand = null;
         }

      }

      public int getHighestNote() {
         int highest = this.midiNotes[0];

         for(int i = 1; i < this.midiNotes.length; ++i) {
            if (this.midiNotes[i] > highest) {
               highest = this.midiNotes[i];
            }
         }

         return highest;
      }

      public int getLowestNote() {
         int lowest = this.midiNotes[0];

         for(int i = 1; i < this.midiNotes.length; ++i) {
            if (this.midiNotes[i] < lowest) {
               lowest = this.midiNotes[i];
            }
         }

         return lowest;
      }

      public int getDistanceTo(Object o) {
         int i1;
         if (o instanceof Note) {
            Note note = (Note)o;
            i1 = note.midiNumber;
            return Math.min(Math.abs(this.getHighestNote() - note.midiNumber), Math.abs(this.getLowestNote() - note.midiNumber));
         } else {
            ChordRealization c = (ChordRealization)o;
            i1 = Math.abs(this.getHighestNote() - c.getHighestNote());
            int i2 = Math.abs(this.getLowestNote() - c.getHighestNote());
            int i3 = Math.abs(this.getHighestNote() - c.getLowestNote());
            int i4 = Math.abs(this.getLowestNote() - c.getLowestNote());
            int min1 = Math.min(i1, i2);
            int min2 = Math.min(i3, i4);
            return Math.min(min1, min2);
         }
      }
   }
}
