package Mapping;

public class NGram {
   public String name;
   public Chord mappedTo;

   public NGram(String name) {
      this.name = name;
   }

   public void mapTo(Chord chord) {
      this.mappedTo = chord;
   }

   public String toString() {
      return this.name;
   }
}
