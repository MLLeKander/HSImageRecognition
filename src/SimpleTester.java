import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.Graphics2D;
import javax.imageio.*;

public class SimpleTester {
   private static int red(int rgb) {return rgb >> 16 & 0xFF;}
   private static int green(int rgb) {return rgb >> 8 & 0xFF;}
   private static int blue(int rgb) {return rgb & 0xFF;}

   public static BufferedImage filterBoard(boolean rGT, int r, boolean gGT, int g, boolean bGT, int b, BufferedImage img) {
      BufferedImage i = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

      for (int x = 0; x < img.getWidth(); x++) {
         for (int y = 0; y < img.getHeight(); y++) {
            int rgb = img.getRGB(x,y);
            int color = (red(rgb) > r == rGT) && (green(rgb) > g == gGT) && (blue(rgb) > b == bGT) ? 0xFF : 0;
            i.setRGB(x,y,color);
         }
      }

      return i;
   }

   public static void main(String[] args) throws Exception {
      if (args.length != 1) {
         System.err.println("wtf? Need 1 argument...");
         return;
      }

      Scanner scan = new Scanner(System.in);

      File file = new File(args[0]);
      BufferedImage src = ImageIO.read(file);

      while (scan.hasNext()) {
         boolean rGT = scan.next().charAt(0) == '>';
         int r = scan.nextInt();
         boolean gGT = scan.next().charAt(0) == '>';
         int g = scan.nextInt();
         boolean bGT = scan.next().charAt(0) == '>';
         int b = scan.nextInt();

         ImageIO.write(filterBoard(rGT,r,gGT,g,bGT,b,src), "png", new File("out.png"));
         Runtime.getRuntime().exec("gnome-open out.png").waitFor();
      }
   }
}
