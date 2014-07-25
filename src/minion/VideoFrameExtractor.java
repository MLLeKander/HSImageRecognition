import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.sun.jna.Native;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.player.events.MediaPlayerEventType;

public class VideoFrameExtractor {
   public static void main(final String[] args) throws Exception {
      System.setProperty("jna.nosys", "true");
      Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

      //final MediaPlayerFactory mpf = new MediaPlayerFactory();
      final MediaPlayerFactory mpf = new MediaPlayerFactory("--vout", "dummy", "--quiet");
      final HeadlessMediaPlayer hmp = mpf.newHeadlessMediaPlayer();

      String fname = new File(args[0]).getName();
      final String basename = fname.substring(0, fname.lastIndexOf("."));
      hmp.startMedia(args[0]);
      hmp.pause();

      hmp.enableEvents(MediaPlayerEventType.events(MediaPlayerEventType.ALL));
      hmp.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
         int i = 1;
         public void stopped(MediaPlayer mediaPlayer) {
            finish();
         }

         public void finished(MediaPlayer mediaPlayer) {
            finish();
         }

         public void finish() {
            System.out.println("Done here. Bye bye.");
            hmp.release();
            mpf.release();
            System.exit(0);
         }

         public void snapshotTaken(MediaPlayer mp, String filename) {
            //System.out.println("Snapshot taken: "+filename);
            try {
               MinionClassifier.processImage(filename);
               mp.saveSnapshot(new File(String.format("snapOut/%s_%03d.png",basename,i)));
               i++;
            } catch (Exception blah) {
               throw new RuntimeException(blah);
            }
         }
      });

      hmp.play();

      Thread.sleep(200);
      hmp.mute(true);
      Thread.sleep(200);
      hmp.saveSnapshot(new File("snapOut/"+basename+"_000.png"));
   }
}
