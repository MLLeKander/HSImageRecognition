import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.imageio.*;

class MinionClassifier {
   private final static byte BG = 0, FG1 = 1, FG2 = 2;
   private final static byte[] YELLOW = new byte[]{0,-1,-1};
   private final static byte[] GREEN = new byte[]{0,-1,0};

   private static boolean bound(int min, int x, int max) { return min <= x && max > x; }

   private static BufferedImage boardToImage(byte[][] board) {
      BufferedImage i = new BufferedImage(board.length, board[0].length,BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == BG) {
               i.setRGB(x,y,0);
            } else if (board[x][y] == FG1) {
               i.setRGB(x,y,0xFF);
            } else if (board[x][y] == FG2) {
               i.setRGB(x,y,0xFF00);
            }
         }
      }
      return i;
   }

   private static RectRegion fill(byte[][] board, int x, int y, byte mark) {
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

   public static Map<String, int[][]> minionToHash = null;
   public static int badMinionCount = 1;

   public static void loadImageHash() throws Exception {
      loadImageHash("minionBank");
   }

   public static void loadImageHash(String minionDirectoryLocation) throws Exception {
      File minionDirectory = new File(minionDirectoryLocation);
      minionToHash = new HashMap<String, int[][]>();

      for (File minionFile : minionDirectory.listFiles()) {
         BufferedImage minionImage = ImageIO.read(minionFile);

         String fname = minionFile.getName();
         String minionName = fname.substring(0,fname.lastIndexOf("."));

         minionToHash.put(minionName, ImageHash.partitionAverage(minionImage));
      }
   }

   public static void processImage(String fileName) throws Exception {
      if (minionToHash == null) {
         loadImageHash();
      }
      System.out.print("Processing "+fileName);

      File file = new File(fileName);

      BufferedImage src = ImageIO.read(file);

      String name = file.getName();
      String basename = name.substring(0,name.lastIndexOf("."));

      List<RectRegion> minionRegions = getMinionRegions(src);

      System.out.println(" : "+minionRegions.size()+" minions found.");

      int minionCount = 1;
      BufferedImage drawCopy = copyImage(src);
      Graphics2D gfx = drawCopy.createGraphics();
      gfx.setStroke(new BasicStroke(3));
      gfx.setPaint(Color.RED);
      gfx.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
      FontMetrics fontMetrics = gfx.getFontMetrics();

      for (RectRegion r : minionRegions) {
         BufferedImage subImage = src.getSubimage(r.minX, r.minY, r.maxX-r.minX, r.maxY-r.minY);

         int[][] myHash = ImageHash.partitionAverage(subImage);
         long closestDifference = Long.MAX_VALUE;
         String closestMinion = "NONE";
         for (String minionName : minionToHash.keySet()) {
            int[][] minionHash = minionToHash.get(minionName);
            long difference = ImageHash.colorDifference(myHash, minionHash);
            if (difference < closestDifference) {
               closestDifference = difference;
               closestMinion = minionName;
            }
         }

         System.out.printf("%7d - %s. ", closestDifference, closestMinion);

         double modifier = 1.0;
         if (closestMinion.contains("_buff"))
            modifier *= 2.0;
         if (closestMinion.contains("_golden"))
            modifier *= 2.5;
         if (closestMinion.contains("_conceal"))
            modifier *= 2.0;
         if (closestMinion.contains("_focus"))
            modifier *= 1.5;
         if (closestMinion.contains("_windfury"))
            modifier *= 1.25;

         boolean error = closestDifference > modifier*200000;

         if (error) {
            File ofile = new File(String.format("badMinionOut/%04d.png",badMinionCount));
            System.out.println("Not good enough. Writing to "+ofile.getAbsolutePath());
            ImageIO.write(subImage, "png", ofile);
            minionToHash.put(badMinionCount+"", myHash);
            badMinionCount++;

            if (closestDifference < modifier*800000)
               gfx.setPaint(Color.YELLOW);
            else
               gfx.setPaint(Color.RED);
         } else {
            System.out.println("Good enough for me.");
            if (closestMinion.matches("\\d+")) {
               File ofile = new File(String.format("badMinionOut/%s_%s.png",closestMinion,basename));
               System.out.println(" Duplicate. Writing to "+ofile.getAbsolutePath());
               ImageIO.write(subImage, "png", ofile);
            }
            gfx.setPaint(Color.GREEN);
         }

         minionCount++;

         int underscoreNdx = closestMinion.indexOf('_');
         String minionBaseName = underscoreNdx>0?closestMinion.substring(0,underscoreNdx):closestMinion;

         gfx.draw(r.toRectangle());
         int sw = fontMetrics.stringWidth(minionBaseName);
         gfx.drawString(minionBaseName, (r.minX+r.maxX-sw)/2, r.minY-3);
      }
      ImageIO.write(drawCopy, "png", new File("annotationOut/"+basename+".png"));
   }

   public static BufferedImage copyImage(BufferedImage in) {
      ColorModel cm = in.getColorModel();
      boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
      WritableRaster raster = in.copyData(null);
      return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
   }

   public static void main(String[] args) throws Exception {
      for (String arg : args) {
         processImage(arg);
      }
   }

   private static int red(int rgb) {return rgb >> 16 & 0xFF;}
   private static int green(int rgb) {return rgb >> 8 & 0xFF;}
   private static int blue(int rgb) {return rgb & 0xFF;}

   public static boolean isBoardPixel(int pixel) {
      return red(pixel) > 130 && green(pixel) > 100 && blue(pixel) < 150;
   }

   public static boolean isAttackPixel(int pixel) {
      return green(pixel) > 170;
   }

   public static List<RectRegion> getMinionRegions(BufferedImage img) throws Exception {
      byte[][] board = new byte[img.getWidth()][img.getHeight()];
      ImageIO.write(img, "png", new File("devout/inter0.png"));

      // Segment board according to isBoardPixel. Board pixels => FG1, else => BG
      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            board[x][y] = isAttackPixel(img.getRGB(x,y)) ? FG1 : BG;
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/attackPixel.png"));

      // Segment board according to isBoardPixel. Board pixels => FG1, else => BG
      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            board[x][y] = isBoardPixel(img.getRGB(x,y)) ? FG1 : BG;
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/inter1.png"));

      // Find largest FG1 region. FG1 => FG2
      RectRegion maxR = new RectRegion();
      int maxX = 0, maxY = 0;
      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == FG1) {
               RectRegion tmp = fill(board, x, y, FG2);
               if (tmp.pixels > maxR.pixels) {
                  maxR = tmp;
                  maxX = x;
                  maxY = y;
               }
            }
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/inter2.png"));

      // Largest region FG2 => FG1
      fill(board, maxX, maxY, FG1);
      ImageIO.write(boardToImage(board), "png", new File("devout/inter3.png"));

      // Turn all non-board regions back to black
      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == FG2) {
               board[x][y] = BG;
            }
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/inter4.png"));

      // Clear outside black region, so we can look at minion "islands"
      fill(board, 1, 1, FG1);
      ImageIO.write(boardToImage(board), "png", new File("devout/inter5.png"));

      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == BG) {
               int d = img.getRGB(x,y);
               //if ((d & 0xFF) > 130 && (d >> 8 & 0xFF) > 130 && (d >> 16 & 0xFF) > 130) {
               if (isAttackPixel(img.getRGB(x,y))) {
                  board[x][y] = FG1;
               }
            }
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/inter6.png"));

      List<RectRegion> regions = new ArrayList<RectRegion>();
      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == BG) {
               RectRegion region = fill(board,x,y,FG2);
               if (region.pixels > 500 && region.height() > region.width()) {
                  regions.add(region);
               }
            }
         }
      }

      return regions;
   }
}
