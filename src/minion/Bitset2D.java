import java.util.BitSet;

class BitSet2D {
   private final int r, c;
   private final BitSet bitset;

   public BitSet2D(int rows, int cols) {
      r = rows;
      c = cols;
      bitset = new BitSet(r*c);
   }

   public void set(int row, int col) {
      bitset.set(getIndex(row,col));
   }

   public void clear(int row, int col) {
      bitset.clear(getIndex(row,col));
   }

   public void get(int row, int col) {
      return bitset.get(getIndex(row,col));
   }

   private int getIndex(int row, int col) {
      return row*cols + col;
   }
}
