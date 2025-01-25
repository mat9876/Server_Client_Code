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

public class LineEndings {
    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 100;
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = LineEndings.class.getResourceAsStream("testconfig.properties");
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
    public void tc21EnterFollowedByBROADCASTWithWindowsLineEndingsReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        String message = Utils.objectToMessage(new Enter("myname")) + "\r\n" +
                Utils.objectToMessage(new BroadcastReq("a")) + "\r\n";
        out.print(message);
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", broadcastResp.status());
    }

    @Test
    public void tc22EnterFollowedByBROADCASTWithLinuxLineEndingsReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        String message = Utils.objectToMessage(new Enter("myname")) + "\n" +
                Utils.objectToMessage(new BroadcastReq("a")) + "\n";
        out.print(message);
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", broadcastResp.status());
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}