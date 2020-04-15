package Mapping;

public enum Hand {
   LEFT(21, 64),
   RIGHT(57, 108);

   private final int lowerBound;
   private final int upperBound;

   public static Hand getOpposite(Hand hand) {
      return hand.equals(RIGHT) ? LEFT : RIGHT;
   }

   private Hand(int lowerBound, int upperBound) {
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
   }

   public double upperBound() {
      return (double)this.upperBound;
   }

   public double lowerBound() {
      return (double)this.lowerBound;
   }
}
