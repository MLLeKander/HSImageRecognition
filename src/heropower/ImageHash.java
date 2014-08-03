import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;

class ImageHash {
   public static int red(int c) {return c >> 16 & 0xFF;}
   public static int green(int c) {return c >> 8 & 0xFF;}
   public static int blue(int c) {return c & 0xFF;}

   public static String colorToString(int c) {
      return String.format("(%02X,%02X,%02X)",red(c),green(c),blue(c));
   }

   public static int averageColor(BufferedImage src, int minCol, int minRow, int maxCol, int maxRow) {
      long r = 0, g = 0, b = 0;
      for (int x = minCol; x < maxCol; x++) {
         for (int y = minRow; y < maxRow; y++) {
            int d = src.getRGB(x, y);
            r += red(d);
            g += green(d);
            b += blue(d);
         }
      }
      int size = (maxCol-minCol)*(maxRow-minRow);
      int r_c = (int)r/size;
      int g_c = (int)g/size;
      int b_c = (int)b/size;

      assert(r_c <= 0xFF);
      assert(g_c <= 0xFF);
      assert(b_c <= 0xFF);

      return r_c << 16 | g_c << 8 | b_c;
   }

   public static int[][] partitionAverage(BufferedImage src) {
      return partitionAverage(src, 10, 10);
   }

   public static int[][] partitionAverage(BufferedImage src, int cols, int rows) {
      int[][] out = new int[cols][rows];
      for (int col = 0; col < cols; col++) {
         for (int row = 0; row < rows; row++) {
            int fromCol = col*src.getWidth()/cols;
            int fromRow = row*src.getHeight()/rows;
            int toCol = (col+1)*src.getWidth()/cols;
            int toRow = (row+1)*src.getHeight()/rows;
            out[col][row] = averageColor(src, fromCol, fromRow, toCol, toRow);
         }
      }
      return out;
   }

   public static long colorDifference(int[][] a, int[][] b) {
      assert(a.length == b.length);
      assert(a[0].length == b[0].length);

      long colorDiff = 0;
      for (int x = 0; x < a.length; x++) {
         for (int y = 0; y < a[0].length; y++) {
            int a_c = a[x][y];
            int b_c = b[x][y];
            colorDiff += Math.pow(Math.abs(red(a_c)-red(b_c)),2);
            colorDiff += Math.pow(Math.abs(green(a_c)-green(b_c)),2);
            colorDiff += Math.pow(Math.abs(blue(a_c)-blue(b_c)),2);
         }
      }
      return colorDiff;
   }

   public static void main(String[] args) throws Exception {
      List<int[][]> averages = new ArrayList<int[][]>(args.length);
      for (String arg : args) {
         File file = new File(arg);
         BufferedImage src = ImageIO.read(file);
         int[][] average = partitionAverage(src, 10, 10);
         averages.add(average);
      }
      for (int[][] a : averages) {
         for (int[][] b : averages) {
            System.out.printf(" %7d", colorDifference(a,b));
         }
         System.out.println();
      }
   }
}
