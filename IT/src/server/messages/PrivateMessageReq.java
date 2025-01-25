package server.messages;

public record PrivateMessageReq(String recipient, String message) {
}
