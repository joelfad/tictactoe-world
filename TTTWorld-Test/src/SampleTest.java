import me.benthomas.tttworld.client.ui.MainFrame;
import me.benthomas.tttworld.server.Server;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.*;

import javax.swing.SwingUtilities;
import java.io.*;
import java.util.Properties;

/**
 * A simple class to test out AssertJ Swing on the Tic-Tac-Toe-World client.
 */
public class SampleTest {
    private FrameFixture window;
    private Server server;
    private Thread serverThread;
    private static final int BUFFERSIZE = 4096;

    public static void copyResource(String resName, String newPath) throws IOException {
        // create input stream
        try (InputStream is = SampleTest.class.getResourceAsStream(resName)) {

            // create output stream
            try (OutputStream os = new FileOutputStream(newPath)) {

                byte[] bytes = new byte[BUFFERSIZE];
                int size;   // the number of bytes you read in each time

                while ((size = is.read(bytes)) > 0) {
                    os.write(bytes, 0, size);
                }
            }
        }
    }

    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public void setUp() throws Exception {
        copyResource("/accounts.db", "accounts.db");
        copyResource("/hosts.txt", "hosts.txt");
        copyResource("/server.crt", "server.crt");
        copyResource("/server.pk8", "server.pk8");

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server = new Server(new Properties());
                try {
                    server.start(15060);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        serverThread.start();

        SwingUtilities.invokeAndWait(new Runnable () {
            @Override
            public void run() {
                MainFrame mf = new MainFrame();
                window = new FrameFixture(mf);
            }
        });
    }

    @Test
    public void simpleTest_01() throws Exception { // for Thread.sleep()
        SwingUtilities.invokeAndWait(new Runnable () {
            @Override
            public void run() {
                ((MainFrame)window.target()).displayServerSelectDialog();
            }
        });

        Thread.sleep(10000);    // wait 10 seconds
    }

    @After
    public void tearDown() throws Exception {
        window.cleanUp();
        server.stop();
        serverThread.join();

        // remove temporary files
        new File("accounts.db").delete();
        new File("hosts.txt").delete();
        new File("server.crt").delete();
        new File("server.pk8").delete();
    }
}