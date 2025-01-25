package Client.interfaces;

public interface ClientHeaderCommands {
    String ENTER = "ENTER";
    String PING = "PING";
    String PONG = "PONG";
    String PONG_ERROR = "PONG_ERROR";
    String CHALLENGE_NOTIFICATION = "CHALLENGE_NOTIFICATION";
    String PARSE_ERROR = "PARSE_ERROR";
    String ERROR = "ERROR";
    String USER_LIST_REQ = "USER_LIST_REQ";
    String DENIED = "DENIED";
    String CHALLENGE_RESP = "CHALLENGE_RESP";
    String FILE_UPLOAD_NOTIFICATION = "FILE_UPLOAD_NOTIFICATION";
    String BROADCAST_REQ = "BROADCAST_REQ";
    String PRIVATE_MESSAGE_REQ = "PRIVATE_MESSAGE_REQ";
    String CHALLENGE_REQ = "CHALLENGE_REQ";
    String JOINED = "JOINED";
    String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    String PRIVATE_MESSAGE_RESP = "PRIVATE_MESSAGE_RESP";
    String BROADCAST = "BROADCAST";
    String BROADCAST_RESP = "BROADCAST_RESP";
    String CHALLENGE_START = "CHALLENGE_START";
    String CHALLENGE_MOVE = "CHALLENGE_MOVE";
    String CHALLENGE_COMPLETE_NOTIFICATION = "CHALLENGE_COMPLETE_NOTIFICATION";
    String ROCK = "ROCK";
    String PAPER = "PAPER";
    String SCISSORS = "SCISSORS";
    String FILE_TRANSFER = "FILE_TRANSFER";
}
