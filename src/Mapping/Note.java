package Mapping;

public class Note {
   public String name;
   public int octave;
   public int midiNumber;
   public Hand hand;
   public Letter mappedTo;

   public Note(String name, int octave, Letter letter, int number) {
      this.name = name;
      this.octave = octave;
      this.mappedTo = letter;
      this.midiNumber = number;
      if (number == -1) {
         this.hand = null;
      } else if ((double)number >= Hand.LEFT.upperBound()) {
         this.hand = Hand.RIGHT;
      } else if ((double)number <= Hand.RIGHT.lowerBound()) {
         this.hand = Hand.LEFT;
      } else {
         this.hand = null;
      }

   }

   public String toString() {
      return this.name + this.octave;
   }

   public int getDistanceTo(Object o) {
      if (this.name.equals("SPACE")) {
         System.err.println("you called getDistanceTo on the space sign, something wrong");
      }

      if (o instanceof Note) {
         Note note = (Note)o;
         int midiNote2 = note.midiNumber;
         return Math.abs(midiNote2 - this.midiNumber);
      } else {
         Chord.ChordRealization chord = (Chord.ChordRealization)o;
         return Math.min(Math.abs(chord.getHighestNote() - this.midiNumber), Math.abs(chord.getLowestNote() - this.midiNumber));
      }
   }
}
