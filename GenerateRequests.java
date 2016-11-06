import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class GenerateRequests implements Runnable {
  public static void main(String[] args) throws Exception {
    String            rawUrl     = (args.length > 0 ? args[0] : "http://localhost:4567/ping");
    URL               url        = new URL(rawUrl);
    GenerateRequests  requester  = new GenerateRequests(url);

    long              startTime  = System.nanoTime();
    ExecutorService   threadPool = Executors.newFixedThreadPool(20);

    for(int i = 0; i < 20; ++i) { threadPool.execute(requester); }
    threadPool.shutdown();
  }

  private URL       url;
  private int       count;
  private long      startTime;
  private Semaphore countSemaphore;


  public GenerateRequests(URL url) {
    this.count          = 0;
    this.countSemaphore = new Semaphore(1);
    this.startTime      = System.nanoTime();
    this.url            = url;
  }

  public void run() {
    try {
      int responseCode = ((HttpURLConnection)url.openConnection()).getResponseCode();
      if(responseCode != 200) {
        System.err.println("Expected 200 response, got " + responseCode);
        System.exit(1);
      }
      float seconds = (System.nanoTime() - startTime) / 1000000000L;
      countSemaphore.acquire();
      ++count;
      System.out.println("Request " + count + " responded after " + seconds);
      countSemaphore.release();
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (InterruptedException e) {
      countSemaphore.release();
    }
  }
}
