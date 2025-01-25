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

public class PacketBreakup {
    private final static Properties PROPS = new Properties();
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;
    private final static int MAX_DELTA_ALLOWED_MS = 100;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = PacketBreakup.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    public void setup() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            try {
                s = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new PrintWriter(s.getOutputStream(), true);
                return;
            } catch (IOException e) {
                Thread.sleep(100); // Wait before retrying
            }
        }
    }

    @AfterEach
    public void cleanup() throws IOException {
        s.close();
    }

    @Test
    public void tc41IdentFollowedByBroadcastWithMultipleFlushReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        out.print("ENTER {\"username\":\"m");
        out.flush();
        out.print("yname\"}\r\nBROAD");
        out.flush();
        out.print("CAST_REQ {\"message\":\"a\"}\r\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", broadcastResp.status());
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}