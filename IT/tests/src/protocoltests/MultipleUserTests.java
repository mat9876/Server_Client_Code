package src.protocoltests;
import com.fasterxml.jackson.core.*;
import org.junit.jupiter.api.*;
import src.protocoltests.protocol.messages.*;
import src.protocoltests.protocol.utils.*;
import java.io.*;
import java.net.*;
import java.util.*;
import static java.time.Duration.*;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleUserTests {
    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 100;
    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = MultipleUserTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    public void setup() throws IOException {
        socketUser1 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);
        socketUser2 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
    }

    @Test
    public void tc31JoinedIsReceivedByOtherUserWhenUserConnects() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2);
        String resIdent = receiveLineWithTimeout(inUser1);
        Joined joined = Utils.messageToObject(resIdent);
        assertEquals(new Joined("user2"), joined);
    }

    @Test
    public void tc32BroadcastMessageIsReceivedByOtherConnectedClients() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);
        outUser1.println(Utils.objectToMessage(new BroadcastReq("messagefromuser1")));
        outUser1.flush();
        String fromUser1 = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp1 = Utils.messageToObject(fromUser1);
        assertEquals("OK", broadcastResp1.status());
        String fromUser2 = receiveLineWithTimeout(inUser2);
        Broadcast broadcast2 = Utils.messageToObject(fromUser2);
        assertEquals(new Broadcast("user1", "messagefromuser1"), broadcast2);
        outUser2.println(Utils.objectToMessage(new BroadcastReq("messagefromuser2")));
        outUser2.flush();
        fromUser2 = receiveLineWithTimeout(inUser2);
        BroadcastResp broadcastResp2 = Utils.messageToObject(fromUser2);
        assertEquals("OK", broadcastResp2.status());
        fromUser1 = receiveLineWithTimeout(inUser1);
        Broadcast broadcast1 = Utils.messageToObject(fromUser1);
        assertEquals(new Broadcast("user2", "messagefromuser2"), broadcast1);
    }

    @Test
    public void tc33EnterMessageWithAlreadyConnectedUsernameReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        outUser2.println(Utils.objectToMessage(new Enter("user1")));
        outUser2.flush();
        String resUser2 = receiveLineWithTimeout(inUser2);
        EnterResp enterResp = Utils.messageToObject(resUser2);
        assertEquals(new EnterResp("ERROR", 5000), enterResp);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}