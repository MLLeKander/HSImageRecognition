import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.imageio.*;
import javax.swing.*;

interface PixelConstraint {
   public boolean matchPixel(int rgb);
   public void init();
}

class PixelConstraintChooser extends JComponent implements PixelConstraint {
   private static int red(int rgb) {return rgb >> 16 & 0xFF;}
   private static int green(int rgb) {return rgb >> 8 & 0xFF;}
   private static int blue(int rgb) {return rgb & 0xFF;}
   private static int rgb(int red, int blue, int green) {
      return red << 16 | blue << 8 | green;
   }

   final JTextField[] minTextFields = new JTextField[3];
   final JTextField[] maxTextFields = new JTextField[3];
   private int rMin, gMin, bMin, rMax, gMax, bMax;

   private static GridBagConstraints constraints(int x, int y) {
      return setConstraints(x,y,new GridBagConstraints());
   }
   private static GridBagConstraints setConstraints(int x, int y, GridBagConstraints c) {
      c.gridx = x;
      c.gridy = y;
      return c;
   }

   public PixelConstraintChooser() {
      super();
      this.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;

      for (int i = 0; i < 3; i++) {
         minTextFields[i] = new JTextField("0", 3);
         this.add(minTextFields[i], setConstraints(i,0,c));
      }

      String labelNames[] = new String[]{"r", "g", "b"};
      for (int i = 0; i < 3; i++) {
         this.add(new JLabel(labelNames[i]+"", JLabel.CENTER), setConstraints(i,1,c));
      }

      for (int i = 0; i < 3; i++) {
         maxTextFields[i] = new JTextField("256", 3);
         this.add(maxTextFields[i], setConstraints(i,2,c));
      }
   }

   public void init() {
      rMin = Integer.parseInt(minTextFields[0].getText());
      gMin = Integer.parseInt(minTextFields[1].getText());
      bMin = Integer.parseInt(minTextFields[2].getText());
      rMax = Integer.parseInt(maxTextFields[0].getText());
      gMax = Integer.parseInt(maxTextFields[1].getText());
      bMax = Integer.parseInt(maxTextFields[2].getText());
      System.out.println("("+rMin+"-"+rMax+","+gMin+"-"+gMax+","+bMin+"-"+bMax+")");
   }

   private static boolean bound(int low, int d, int high) {return low <= d && d <= high;}

   public boolean matchPixel(int rgb) {
      return bound(rMin, red(rgb), rMax) && bound(gMin, green(rgb), gMax) && bound(bMin, blue(rgb), bMax);
   }
}

public class SimpleSwingTester {
   private static int red(int rgb) {return rgb >> 16 & 0xFF;}
   private static int green(int rgb) {return rgb >> 8 & 0xFF;}
   private static int blue(int rgb) {return rgb & 0xFF;}
   private static int rgb(int red, int blue, int green) {
      return red << 16 | blue << 8 | green;
   }


   private static GridBagConstraints setConstraints(int x, int y, int width, GridBagConstraints c) {
      c.gridx = x;
      c.gridy = y;
      c.gridwidth = width;
      return c;
   }

   private static boolean anyMatch(Collection<PixelConstraint> constraints, int data) {
      for (PixelConstraint c : constraints)
         if (c.matchPixel(data))
            return true;
      return false;
   }

   public static BufferedImage filterBoard(Collection<PixelConstraint> constraints, BufferedImage src) {
      BufferedImage i = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

      for (PixelConstraint c : constraints)
         c.init();

      for (int x = 0; x < src.getWidth(); x++) {
         for (int y = 0; y < src.getHeight(); y++) {
            int rgb = src.getRGB(x,y);
            int color = anyMatch(constraints, rgb) ? 0 : 0xBF;
            i.setRGB(x,y,color+rgb(red(rgb)/3, green(rgb)/3, blue(rgb)/4));
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
      final BufferedImage src = ImageIO.read(file);

      final JLabel imageLabel = new JLabel(new ImageIcon(src));
      final JTextField pixelOut = new JTextField(8);
      final PixelConstraintChooser pcc1 = new PixelConstraintChooser(), pcc2 = new PixelConstraintChooser(), pcc3 = new PixelConstraintChooser();
      JButton filterSubmit = new JButton("Filter");

      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
      inputPanel.add(pixelOut);
      inputPanel.add(pcc1);
      inputPanel.add(new JLabel("OR"));
      inputPanel.add(pcc2);
      inputPanel.add(new JLabel("OR"));
      inputPanel.add(pcc3);
      inputPanel.add(filterSubmit);

      imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
         public void mouseMoved(MouseEvent e) {
            int rgb = src.getRGB(e.getX(),e.getY());
            pixelOut.setText(red(rgb)+","+green(rgb)+","+blue(rgb));
         }
      });

      filterSubmit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Collection<PixelConstraint> c = Arrays.asList((PixelConstraint)pcc1, pcc2, pcc3);
            imageLabel.setIcon(new ImageIcon(filterBoard(c, src)));
            System.out.println("----");
         }
      });

      JPanel panel = new JPanel();
      panel.add(imageLabel);
      panel.add(inputPanel);

      JFrame frame = new JFrame();
      frame.add(panel);
      frame.setTitle("title");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
}
