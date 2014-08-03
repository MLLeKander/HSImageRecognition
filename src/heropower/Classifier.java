import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class Classifier {
   protected static RectRegion fill(byte[][] board, int x, int y, byte mark) {
      byte target = board[x][y];
      int count = 0;
      ArrayList<Integer> queueX = new ArrayList<Integer>();
      ArrayList<Integer> queueY = new ArrayList<Integer>();
      RectRegion region = new RectRegion();

      if (target == mark)
         throw new IllegalArgumentException("Target cannot be equal to mark");
      board[x][y] = mark;

      queueX.add(x);
      queueY.add(y);

      //TODO: Maybe change this to DFS for efficiency?
      while (!queueX.isEmpty()) {
         int rt = queueX.remove(queueX.size()-1);
         int ct = queueY.remove(queueY.size()-1);
         for (int i = -1; i <= 1; i++) {
            for (int o = -1; o <= 1; o++) {
               if (bound(0,rt+i,board.length) && bound(0,ct+o,board[0].length) &&
                     board[rt+i][ct+o] == target) {
                  board[rt+i][ct+o] = mark;
                  queueX.add(rt+i);
                  queueY.add(ct+o);
                  region.addPoint(rt+i,ct+o);
               }
            }
         }
      }
      return region;
   }

   protected static BufferedImage copyImage(BufferedImage in) {
      ColorModel cm = in.getColorModel();
      boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
      WritableRaster raster = in.copyData(null);
      return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
   }

   protected static boolean bound(double min, double x, double max) { return min <= x && max > x; }
   protected static boolean bound(int min, int x, int max) { return min <= x && max > x; }
   protected static boolean approxEq(double a, double b, double err) {return bound(a*(1-err),b,a*(1+err));}
   protected static boolean approxEq(double a, double b) {return approxEq(a,b,.025);}

   protected static int red(int rgb) {return rgb >> 16 & 0xFF;}
   protected static int green(int rgb) {return rgb >> 8 & 0xFF;}
   protected static int blue(int rgb) {return rgb & 0xFF;}
}
