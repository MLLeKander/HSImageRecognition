import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.imageio.*;

public class HeroPowerClassifier extends Classifier {
   private final static byte BG = 0, FG1 = 1, FG2 = 2;
   private final static byte[] YELLOW = new byte[]{0,-1,-1};
   private final static byte[] GREEN = new byte[]{0,-1,0};

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

   public static Map<String, int[][]> heroPowerToHash = null;
   public static int badHeroPowerCount = 1;

   public static void loadImageHash() throws Exception {
      loadImageHash("heroPowerBank");
   }

   public static void loadImageHash(String heroPowerDirectoryLocation) throws Exception {
      File heroPowerDirectory = new File(heroPowerDirectoryLocation);
      heroPowerToHash = new HashMap<String, int[][]>();

      for (File heroPowerFile : heroPowerDirectory.listFiles()) {
         BufferedImage heroPowerImage = ImageIO.read(heroPowerFile);

         String fname = heroPowerFile.getName();
         String heroPowerName = fname.substring(0,fname.lastIndexOf("."));

         heroPowerToHash.put(heroPowerName, ImageHash.partitionAverage(heroPowerImage, 5, 5));
      }
   }

   public static void main(String[] args) throws Exception {
      for (String arg : args) {
         processImage(arg);
      }
   }

   public static void processImage(String fileName) throws Exception {
      if (heroPowerToHash == null) {
         loadImageHash();
      }
      System.out.print("Processing "+fileName);

      File file = new File(fileName);

      BufferedImage src = ImageIO.read(file);

      String name = file.getName();
      String basename = name.substring(0,name.lastIndexOf("."));

      List<RectRegion> heroPowerRegions = getHeroPowerRegions(src);

      System.out.println(" : "+heroPowerRegions.size()+" hero powers found.");

      int heroPowerCount = 1;
      BufferedImage drawCopy = copyImage(src);
      Graphics2D gfx = drawCopy.createGraphics();
      gfx.setStroke(new BasicStroke(3));
      gfx.setPaint(Color.RED);
      gfx.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
      FontMetrics fontMetrics = gfx.getFontMetrics();

      for (RectRegion r : heroPowerRegions) {
         BufferedImage subImage = src.getSubimage(r.minX, r.minY, r.maxX-r.minX, r.maxY-r.minY);

         int[][] myHash = ImageHash.partitionAverage(subImage, 5, 5);
         long closestDifference = Long.MAX_VALUE;
         String closestHeroPower = "NONE";
         for (String heroPowerName : heroPowerToHash.keySet()) {
            int[][] heroPowerHash = heroPowerToHash.get(heroPowerName);
            long difference = ImageHash.colorDifference(myHash, heroPowerHash);
            if (difference < closestDifference) {
               closestDifference = difference;
               closestHeroPower = heroPowerName;
            }
         }

         System.out.printf("%7d - %s. ", closestDifference, closestHeroPower);

         if (closestDifference > 10000) {
            File ofile = new File(String.format("badHeroPowerOut/%04d.png",badHeroPowerCount++));
            ImageIO.write(subImage, "png", ofile);
            System.out.println("Not good enough. Writing to "+ofile.getAbsolutePath());
         } else {
            System.out.println("Good enough for me.");
         }

         gfx.draw(r.toRectangle());
         String heroPowerBaseName = closestHeroPower;
         int sw = fontMetrics.stringWidth(heroPowerBaseName);
         gfx.drawString(heroPowerBaseName, (r.minX+r.maxX-sw)/2, r.minY-3);
      }
      ImageIO.write(drawCopy, "png", new File("annotationOut/"+basename+".png"));
   }

   public static boolean isHeroPowerBorderPixel(int pixel) {
      return Classifier.red(pixel) < 100 && green(pixel) < 80 && blue(pixel) < 60;
   }

   static int devCount = 0;
   public static List<RectRegion> getHeroPowerRegions(BufferedImage img) throws Exception {
      byte[][] board = new byte[img.getWidth()][img.getHeight()];

      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            //board[x][y] = isHeroPowerBorderPixel(img.getRGB(x,y)) ? FG1 : BG;
            int d = img.getRGB(x,y), r=red(d), g=green(d), b=blue(d);
            board[x][y] = (r > 100 || g > 80 || b > 60) && (b < r || b < g) ? FG1 : BG;
         }
      }
      ImageIO.write(boardToImage(board), "png", new File("devout/inter"+devCount++ +".png"));

      List<RectRegion> regions = new ArrayList<RectRegion>();
      for (int x = 0; x < board.length; x++) {
         for (int y = 0; y < board[0].length; y++) {
            if (board[x][y] == FG1) {
               RectRegion tmp = fill(board, x, y, FG2);
               if (
                     approxEq(tmp.width(), tmp.height(), .1) &&
                     approxEq(tmp.height(), img.getHeight()/10, .2) &&
                     tmp.width() > 5 && tmp.height() > 5 &&
                     bound(img.getWidth()/2, tmp.minX, 2*img.getWidth()/3) &&
                     true
                  ) {
                  regions.add(tmp);
               }
            }
         }
      }

      return regions;
   }
}
