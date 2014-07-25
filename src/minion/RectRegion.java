import static java.lang.Math.*;
import java.awt.Rectangle;

class RectRegion {
   int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE,
       minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
   int pixels = 0;

   public void addPoint(int x, int y) {
      pixels++;
      minX = min(minX, x);
      maxX = max(maxX, x);
      minY = min(minY, y);
      maxY = max(maxY, y);
   }

   public int height(){return maxY-minY;}
   public int width(){return maxX-minX;}
   public Rectangle toRectangle(){return new Rectangle(minX, minY, maxX-minX, maxY-minY);}
}
