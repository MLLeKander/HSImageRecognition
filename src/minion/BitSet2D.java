import java.util.BitSet;

class BitSet2D {
   private final int rows, cols;
   private final BitSet bitset;

   public BitSet2D(int rows, int cols) {
      this.rows = rows;
      this.cols = cols;
      bitset = new BitSet(rows*cols);
   }

   public void set(int row, int col) {
      bitset.set(getIndex(row,col));
   }

   public void clear(int row, int col) {
      bitset.clear(getIndex(row,col));
   }

   public boolean get(int row, int col) {
      return bitset.get(getIndex(row,col));
   }

   private int getIndex(int row, int col) {
      return row*cols + col;
   }
}
