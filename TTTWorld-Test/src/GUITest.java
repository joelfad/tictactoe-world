import me.benthomas.tttworld.client.ui.CreatePasswordDialog;
import me.benthomas.tttworld.client.ui.GameFrame;
import me.benthomas.tttworld.client.ui.LoginDialog;
import me.benthomas.tttworld.client.ui.MainFrame;
import me.benthomas.tttworld.server.Server;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.*;
import org.assertj.swing.timing.Pause;

import org.junit.*;

import javax.swing.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * AssertJ Swing GUI tests for the Tic-Tac-Toe-World client.
 */
public class GUITest {
    private FrameFixture window;
    private Robot robot;    // workaround for complex testing
    private Server server;
    private Thread serverThread;
    private static final int BUFFERSIZE = 4096;

    /**
     * Copy resource file to working directory. Used to set up test environment.
     */
    public static void copyResource(String resName, String newPath) throws IOException {
        // create input stream
        try (InputStream is = GUITest.class.getResourceAsStream(resName)) {

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

    /**
     * Automatically select localhost as server
     */
    public void selectServer() {
        window.dialog().textBox("ipField").enterText("127.0.0.1");
        window.dialog().button("OK").click();
    }

    /**
     * Automatically login with valid credentials
     */
    public void login() {
        selectServer();
        DialogFixture loginDialog = window.dialog();
        loginDialog.textBox("usernameField").enterText("bugdetective");
        loginDialog.textBox("passwordField").enterText("iloveseng437");
        loginDialog.button("OK").click();
    }

    /**
     * Automatically disconnect from the server.
     */
    public void disconnect() {
        JMenuItemFixture disconnect = window.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem disconnect) {
                return "Disconnect".equals(disconnect.getText());
            }
        });
        disconnect.click();
    }

    /**
     * Set AssertJ Swing to catch EDT violations.
     */
    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    /**
     * Set up testing environment by copying over resource files, starting the server, and creating the main window.
     */
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

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                MainFrame mf = new MainFrame(false);
                window = new FrameFixture(mf);
                mf.displayServerSelectDialog();
            }
        });

        // use reflection to get access to the robot
        Method m = AbstractComponentFixture.class.getDeclaredMethod("robot");
        m.setAccessible(true);
        robot = (Robot)m.invoke(window);
    }

    /**
     * Test connecting to a running server using its IP address.
     */
    @Test
    public void testSelectValidServer() {
        // setup
        String validIP = "127.0.0.1";
        DialogFixture serverSelect = window.dialog();

        // exercise
        serverSelect.textBox("ipField").enterText(validIP);
        serverSelect.button("OK").click();

        // verify
        DialogFixture result = WindowFinder.findDialog(LoginDialog.class).withTimeout(1000).using(robot);
        result.requireVisible();
    }

    /**
     * Test failing to connect to a server because of an invalid IP address.
     */
    @Test
    public void testSelectInvalidServer() {
        // setup
        String invalidIP = "BatCaveServer";
        DialogFixture serverSelect = window.dialog();

        // exercise
        serverSelect.textBox("ipField").enterText(invalidIP);
        serverSelect.button("OK").click();

        // verify
        GenericTypeMatcher<JOptionPane> matcher = new GenericTypeMatcher<JOptionPane>(JOptionPane.class) {
            protected boolean isMatching(JOptionPane pane) {
                return pane.getMessage() != null &&
                        ((String)pane.getMessage()).startsWith("Failed to connect to the server!") &&
                        pane.isShowing();
            }
        };
        JOptionPaneFixture result = JOptionPaneFinder.findOptionPane(matcher).withTimeout(1000).using(robot);
        result.requireErrorMessage();

        // clean up
        result.button().click();
    }

    /**
     * Test whether an error message is displayed within 30 seconds of disconnecting from the server.
     */
    @Test
    public void testServerTimeout() throws InterruptedException {
        // setup
        selectServer();

        // exercise
        server.stop();

        // verify
        GenericTypeMatcher<JOptionPane> matcher = new GenericTypeMatcher<JOptionPane>(JOptionPane.class) {
            protected boolean isMatching(JOptionPane pane) {
                return pane.getMessage() != null &&
                       ((String)pane.getMessage()).startsWith("Disconnected from server") &&
                       pane.isShowing();
            }
        };
        JOptionPaneFixture result = JOptionPaneFinder.findOptionPane(matcher).withTimeout(30000).using(robot);
        result.requireErrorMessage();

        // clean up
        result.button().click();
    }

    /**
     * Test creating a new user account by confirming the password with an identical value.
     */
    @Test
    public void testCreateNewAccountSamePassword() {
        // setup
        String user = "Batman";
        String pass1 = "I'm Batman";
        String pass2 = pass1;
        selectServer();
        DialogFixture loginDialog = window.dialog();

        // execute
        loginDialog.button("btnNewAccount").click();
        DialogFixture createPasswordDialog = WindowFinder.findDialog(CreatePasswordDialog.class).using(robot);
        createPasswordDialog.textBox("usernameField").enterText(user);
        createPasswordDialog.textBox("passwordField").enterText(pass1);
        createPasswordDialog.textBox("confirmPasswordField").enterText(pass2);
        createPasswordDialog.button("OK").click();
        Pause.pause(100);

        // verify
        window.requireVisible();

        // clean up
        disconnect();
    }

    /**
     * Test not being able to create a new user account because the two password fields don't match.
     */
    @Test
    public void testCreateNewAccountDifferentPassword() {
        // setup
        String user = "Batman";
        String pass1 = "I'm Batman";
        String pass2 = "i'm batman";
        selectServer();
        DialogFixture loginDialog = window.dialog();

        // execute
        loginDialog.button("btnNewAccount").click();
        DialogFixture createPasswordDialog = WindowFinder.findDialog(CreatePasswordDialog.class).using(robot);
        createPasswordDialog.textBox("usernameField").enterText(user);
        createPasswordDialog.textBox("passwordField").enterText(pass1);
        createPasswordDialog.textBox("confirmPasswordField").enterText(pass2);
        createPasswordDialog.button("OK").click();

        // verify
        GenericTypeMatcher<JOptionPane> matcher = new GenericTypeMatcher<JOptionPane>(JOptionPane.class) {
            protected boolean isMatching(JOptionPane pane) {
                return pane.getMessage() == "The passwords you entered did not match!" &&
                       pane.isShowing();
            }
        };
        JOptionPaneFixture result = JOptionPaneFinder.findOptionPane(matcher).withTimeout(1000).using(robot);
        result.requireErrorMessage();
        window.requireNotVisible();

        // clean up
        result.button().click();
        Pause.pause(100);
        createPasswordDialog.close();
        loginDialog.close();
    }

    /**
     * Test logging in with valid credentials.
     */
    @Test
    public void testLoginValidAccount() {
        // setup
        String validUser = "bugdetective";
        String validPass = "iloveseng437";
        selectServer();
        DialogFixture loginDialog = window.dialog();

        // execute
        loginDialog.textBox("usernameField").enterText(validUser);
        loginDialog.textBox("passwordField").enterText(validPass);
        loginDialog.button("OK").click();
        Pause.pause(100);

        // verify
        window.requireVisible();

        // clean up
        disconnect();
    }

    /**
     * Test being denied access because of invalid credentials.
     */
    @Test
    public void testLoginInvalidAccount() {
        // setup
        String validUser = "bugdetective";
        String invalidPass = "password";
        selectServer();
        DialogFixture loginDialog = window.dialog();

        // execute
        loginDialog.textBox("usernameField").enterText(validUser);
        loginDialog.textBox("passwordField").enterText(invalidPass);
        loginDialog.button("OK").click();

        // verify
        GenericTypeMatcher<JOptionPane> matcher = new GenericTypeMatcher<JOptionPane>(JOptionPane.class) {
            protected boolean isMatching(JOptionPane pane) {
                return pane.getMessage() == "The username or password you entered was not recognized!" &&
                       pane.isShowing();
            }
        };
        JOptionPaneFixture result = JOptionPaneFinder.findOptionPane(matcher).withTimeout(1000).using(robot);
        result.requireErrorMessage();
        window.requireNotVisible();

        // clean up
        result.button().click();
        Pause.pause(100);
        loginDialog.close();
    }

    /**
     * Test starting a new game when the server is running.
     */
    @Test
    public void testStartNewGameServerRunning() {
        // setup
        login();

        // execute
        JMenuItemFixture startGame = window.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem startGame) {
                return "Smart".equals(startGame.getText());
            }
        });
        startGame.click();

        // verify
        FrameFixture result = WindowFinder.findFrame(GameFrame.class).withTimeout(1000).using(robot);
        result.requireVisible();

        // clean up
        result.close();
        disconnect();
    }

    /**
     * Test failing to start a new game because the server has been disconnected.
     */
    @Test
    public void testStartNewGameServerNotRunning() {
        // setup
        login();

        // execute
        server.stop();

        // verify
        GenericTypeMatcher<JOptionPane> matcher = new GenericTypeMatcher<JOptionPane>(JOptionPane.class) {
            protected boolean isMatching(JOptionPane pane) {
                return pane.getMessage() != null &&
                        ((String)pane.getMessage()).startsWith("Disconnected from server") &&
                        pane.isShowing();
            }
        };
        JOptionPaneFixture result = JOptionPaneFinder.findOptionPane(matcher).withTimeout(1000).using(robot);
        result.requireErrorMessage();

        // clean up
        result.button().click();
    }

    /**
     * Clean up after each test
     */
    @After
    public void tearDown() throws Exception {
        Pause.pause(100); // wait for 100 ms
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