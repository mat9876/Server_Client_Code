package src.protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import src.protocoltests.protocol.messages.*;
import src.protocoltests.protocol.utils.Utils;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class BroadcastTest {

    private final static Properties PROPS = new Properties();
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = BroadcastTest.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    public void setup() throws IOException {
        s = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        s.close();
    }

    @Test
    public void tc60ValidBroadcastRequest() throws JsonProcessingException {
        receiveLineWithTimeout(in); // ready message
        out.println(Utils.objectToMessage(new Enter("user1")));
        out.flush();
        receiveLineWithTimeout(in);
        BroadcastReq broadcastReq = new BroadcastReq("Hello everyone!");
        out.println(Utils.objectToMessage(broadcastReq));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals(new BroadcastResp("OK", 5000), broadcastResp);
    }

    @Test
    public void tc61BroadcastRequestBeforeEnter() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        BroadcastReq broadcastReq = new BroadcastReq("This should fail!");
        out.println(Utils.objectToMessage(broadcastReq));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        ParseError parseError = Utils.messageToObject(serverResponse);
        assertNotNull(parseError);
    }

    @Test
    public void tc62EmptyBroadcastMessage() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("user1")));
        out.flush();
        receiveLineWithTimeout(in);
        BroadcastReq broadcastReq = new BroadcastReq("   ");
        out.println(Utils.objectToMessage(broadcastReq));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        ParseError parseError = Utils.messageToObject(serverResponse);
        assertNotNull(parseError);
    }

    @Test
    public void tc63BroadcastRequestWithInvalidJson() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("user1")));
        out.flush();
        receiveLineWithTimeout(in);
        out.println("BROADCAST_REQ { invalid json ");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        ParseError parseError = Utils.messageToObject(serverResponse);
        assertNotNull(parseError);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(100), reader::readLine);
    }
}