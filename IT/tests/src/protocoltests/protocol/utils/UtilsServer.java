package src.protocoltests.protocol.utils;
import Client.messages.UserListReq;
import server.messages.UserListResp;
import server.messages.Enter;
import server.messages.EnterResp;
import server.messages.Joined;
import server.messages.Ready;
import server.messages.BroadcastReq;
import server.messages.Pong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import server.messages.PrivateMessageReq;
import server.messages.PrivateMessageResp;
import java.util.HashMap;
import java.util.Map;
import src.protocoltests.protocol.messages.ParseError;

public class UtilsServer {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Enter.class, "ENTER");
        objToNameMapping.put(EnterResp.class, "ENTER_RESP");
        objToNameMapping.put(Joined.class, "JOINED");
        objToNameMapping.put(Ready.class, "READY");
        objToNameMapping.put(BroadcastReq.class, "BROADCAST_REQ");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PrivateMessageReq.class, "PRIVATE_MESSAGE_REQ");
        objToNameMapping.put(PrivateMessageResp.class, "PRIVATE_MESSAGE_RESP");
        objToNameMapping.put(UserListResp.class, "USER_LIST_RESP");
        objToNameMapping.put(UserListReq.class, "USER_LIST_REQ");

        objToNameMapping.put(ParseError.class, "PARSE_ERROR");

    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message: " + clazz.getName());
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
