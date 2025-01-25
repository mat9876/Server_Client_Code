package src.protocoltests;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import server.messages.*;
import src.protocoltests.protocol.utils.*;
import java.util.*;

public class PrivateMessageTests {
    private static final Properties PROPS = new Properties();
    private static final int MAX_DELTA_ALLOWED_MS = 100;
    private Socket client1Socket;
    private BufferedReader client1In;
    private PrintWriter client1Out;
    private Socket client2Socket;
    private BufferedReader client2In;
    private PrintWriter client2Out;

    @BeforeAll
    public static void setupAll() throws IOException {
        InputStream in = PrivateMessageTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    public void setup() throws IOException {
        client1Socket = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        client1In = new BufferedReader(new InputStreamReader(client1Socket.getInputStream()));
        client1Out = new PrintWriter(client1Socket.getOutputStream(), true);

        client2Socket = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        client2In = new BufferedReader(new InputStreamReader(client2Socket.getInputStream()));
        client2Out = new PrintWriter(client2Socket.getOutputStream(), true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        client1Socket.close();
        client2Socket.close();
    }

    @Test
    public void tc71ValidPrivateMessageReturnsAcknowledgment() throws JsonProcessingException {
        receiveLineWithTimeout(client1In); // Ready message for client 1
        client1Out.println(UtilsServer.objectToMessage(new Enter("user1")));
        client1Out.flush();
        receiveLineWithTimeout(client1In); // Response to Enter for client 1

        receiveLineWithTimeout(client2In); // Ready message for client 2
        client2Out.println(UtilsServer.objectToMessage(new Enter("user2")));
        client2Out.flush();
        receiveLineWithTimeout(client2In); // Response to Enter for client 2

        // Sending private message from user1 to user2
        client1Out.println(UtilsServer.objectToMessage(new PrivateMessageReq("user2", "Hello User2!")));
        client1Out.flush();

        String serverResponse;
        do {
            serverResponse = receiveLineWithTimeout(client1In);
        } while (!serverResponse.startsWith("PRIVATE_MESSAGE_RESP"));
        PrivateMessageResp privateMessageResp = UtilsServer.messageToObject(serverResponse);
        assertEquals(new PrivateMessageResp("OK", 7000), privateMessageResp);
    }

    @Test
    public void tc72PrivateMessageToNonExistentUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(client1In); // Ready message for client 1
        client1Out.println(UtilsServer.objectToMessage(new Enter("user1")));
        client1Out.flush();
        receiveLineWithTimeout(client1In); // Response to Enter for client 1

        client1Out.println(UtilsServer.objectToMessage(new PrivateMessageReq("nonexistentUser", "Hello!")));
        client1Out.flush();

        String serverResponse = receiveLineWithTimeout(client1In);
        PrivateMessageResp privateMessageResp = UtilsServer.messageToObject(serverResponse);
        assertEquals(new PrivateMessageResp("ERROR", 7002), privateMessageResp);
    }

    @Test
    public void tc73PrivateMessageWithoutEnteringReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(client1In);
        client1Out.println(UtilsServer.objectToMessage(new PrivateMessageReq("user2", "This will fail")));
        client1Out.flush();
        String serverResponse = receiveLineWithTimeout(client1In);
        PrivateMessageResp privateMessageResp = UtilsServer.messageToObject(serverResponse);
        assertEquals(new PrivateMessageResp("ERROR", 7001), privateMessageResp);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}