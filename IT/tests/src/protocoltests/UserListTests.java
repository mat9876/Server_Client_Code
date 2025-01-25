package src.protocoltests;
import Client.messages.UserListReq;
import com.fasterxml.jackson.core.*;
import org.junit.jupiter.api.*;
import server.messages.UserListResp;
import src.protocoltests.protocol.messages.*;
import src.protocoltests.protocol.utils.*;
import java.io.*;
import java.net.*;
import java.util.*;
import static java.time.Duration.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserListTests {
    private final static Properties PROPS = new Properties();
    private final static int MAX_DELTA_ALLOWED_MS = 100;
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = UserListTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    public  void setup() throws IOException {
        s = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        s.close();
    }

    @Test
    public void tc81UserListReturnsConnectedUsers() throws IOException {
        Socket socket1 = new Socket("localhost", 1337);
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        Socket socket2 = new Socket("localhost", 1337);
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        assertEquals("READY {\"version\":\"1.6.0\"}", receiveLineWithTimeout(in1));
        assertEquals("READY {\"version\":\"1.6.0\"}", receiveLineWithTimeout(in2));
        out1.println(Utils.objectToMessage(new Enter("user1")));
        out1.flush();
        String serverResponse1 = receiveLineWithTimeout(in1);
        EnterResp enterResp1 = Utils.messageToObject(serverResponse1);
        assertNotEquals("PARSE_ERROR", enterResp1);
        out2.println(Utils.objectToMessage(new Enter("user2")));
        out2.flush();
        String serverResponse2 = receiveLineWithTimeout(in2);
        EnterResp enterResp2 = Utils.messageToObject(serverResponse2);
        assertNotEquals("PARSE_ERROR", enterResp2);
        out1.println(UtilsServer.objectToMessage(new UserListReq()));
        out1.flush();
        String userListResponse;
        UserListResp userListResp = null;
        while ((userListResponse = receiveLineWithTimeout(in1)) != null) {
            System.out.println("Received message: " + userListResponse);
            if (userListResponse.startsWith("USER_LIST_RESP")) {
                userListResp = UtilsServer.messageToObject(userListResponse);
                break;
            }
        }
        assertNotNull(userListResp, "USER_LIST_RESP was not received.");
        assertEquals("OK", userListResp.ok());
        assertTrue(userListResp.connectedUsers().contains("user1"), "user1 is not in the connected users list.");
        socket1.close();
        socket2.close();
    }

    @Test
    public void tc82UserListReturnsEmptyListForNoUsers() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(UtilsServer.objectToMessage(new UserListReq()));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        UserListResp userListResp = UtilsServer.messageToObject(serverResponse);
        assertEquals("OK", userListResp.ok());
        assertTrue(userListResp.connectedUsers().isEmpty());
    }

    @Test
    public void tc83UserListRequestAfterUserLeaves() throws IOException {
        receiveLineWithTimeout(in);
        out.println(Utils.objectToMessage(new Enter("user1")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        EnterResp enterResp = Utils.messageToObject(serverResponse);
        assertEquals("OK", enterResp.status());
        out.println(UtilsServer.objectToMessage(new UserListReq()));
        out.flush();
        serverResponse = receiveLineWithTimeout(in);
        UserListResp userListResp = UtilsServer.messageToObject(serverResponse);
        assertTrue(userListResp.connectedUsers().contains("user1"));
        s.close();
        setup();
        receiveLineWithTimeout(in);
        out.println(UtilsServer.objectToMessage(new UserListReq()));
        out.flush();
        serverResponse = receiveLineWithTimeout(in);
        userListResp = UtilsServer.messageToObject(serverResponse);
        assertFalse(userListResp.connectedUsers().contains("user1"));
    }

    @Test
    public void tc84UserListRequestWhenUserNotEntered() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println(UtilsServer.objectToMessage(new UserListReq()));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        UserListResp userListResp = UtilsServer.messageToObject(serverResponse);
        assertEquals("OK", userListResp.ok());
        assertTrue(userListResp.connectedUsers().isEmpty());
    }

    @Test
    void tc85InvalidUserListRequestReturnsParseError() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println("USER_LIST_REQ {\"}");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        ParseError parseError = UtilsServer.messageToObject(serverResponse);
        assertNotNull(parseError);
        assertTrue(serverResponse.contains("PARSE_ERROR"));
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}