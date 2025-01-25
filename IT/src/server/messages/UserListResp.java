package server.messages;
import java.util.Set;

public record UserListResp(String ok, Set<String> connectedUsers) {
}
