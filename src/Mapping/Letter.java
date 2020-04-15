package Mapping;

import java.util.ArrayList;

public class Letter {
   public String name;
   public ArrayList<Note> mappedTo;

   public Letter(String name) {
      this.name = name;
   }

   public void mapTo(ArrayList<Note> notes) {
      this.mappedTo = notes;
   }

   public String toString() {
      return this.name;
   }

   public ArrayList<Note> getTranslationInHand(Hand hand) {
      ArrayList<Note> res = new ArrayList();

      for(int i = 0; i < this.mappedTo.size(); ++i) {
         Note n = (Note)this.mappedTo.get(i);
         if ((double)n.midiNumber <= hand.upperBound() && (double)n.midiNumber >= hand.lowerBound()) {
            res.add(n);
         }
      }

      return res;
   }
}
