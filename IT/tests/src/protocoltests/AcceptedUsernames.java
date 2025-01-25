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

public class AcceptedUsernames {
    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 100;
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;


    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = AcceptedUsernames.class.getResourceAsStream("testconfig.properties");
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
    public void tc11UserNameWithThreeCharactersIsAccepted() throws JsonProcessingException {
        receiveLineWithTimeout(in); //ready message
        out.println(Utils.objectToMessage(new Enter("mym")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
    }

    @Test
    public void tc12UserNameWithTwoCharactersReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("my")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals(new EnterResp("ERROR",5001), enterResp, "Too short username accepted: " + serverResponse);
    }

    @Test
    public void tc13UserNameWith14CharactersIsAccepted() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("abcdefghijklmn")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
    }

    @Test
    public void tc14UserNameWith15CharectersReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("abcdefghijklmop")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals(new EnterResp("ERROR",5001), enterResp, "Too long username accepted: " + serverResponse);
    }

    @Test
    public void tc15UserNameWithStarReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("*a*")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals(new EnterResp("ERROR",5001), enterResp, "Wrong character accepted");
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}