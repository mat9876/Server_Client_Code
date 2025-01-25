package server.handler;
import com.fasterxml.jackson.databind.*;
import server.*;
import server.dataClasses.*;
import server.interfaces.*;
import server.messages.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientHandler implements Runnable, FileServerHeaderCommands, ServerHeaderCommands, ServerHeaderCode, ChallengeServerHeaderCommands {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();
    private final Map<ClientHandler, Boolean> pendingPingMap = new ConcurrentHashMap<>();
    private final Socket socket;
    private Socket fileSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final UsernameServer server;
    private String username;
    private GameSession gameSession;

    public Socket getFileSocket() {
        return fileSocket;
    }

    public UsernameServer getServer() {
        return server;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    public String getUsername() {
        return username;
    }

    public ClientHandler(Socket socket, Socket fileSocket, UsernameServer server) throws IOException {
        this.socket = socket;
        this.fileSocket = fileSocket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.server = server;
    }

    public ClientHandler getClientByUsername(String username) {
        for (ClientHandler client : server.getClients()) {
            if (username.equals(client.getUsername())) {
                return client;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            sendMessage(READY, new Ready("1.6.0"));
            startPingTask();
            String message;
            while ((message = in.readLine()) != null) {
               handleClientMessage(message);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            disconnectClient();
        }
    }

    private void startPingTask() {
        new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    long pingTimeMs = 10000;
                    Thread.sleep(pingTimeMs);
                    sendMessage(PING, new Ping());
                    pendingPingMap.put(this, true);
                } catch (InterruptedException | IOException e) {
                    System.err.println(e.getMessage());
                    break;
                }
            }
        }).start();
    }

    private void handleChallengeMove(String body) {
        try {
            RPSMove move = objectMapper.readValue(body, RPSMove.class);
            System.out.println("Received move from " + move.getUsername() + ": " + move.getMove());
            if (gameSession != null) {
                gameSession.storeMove(this, move);
                gameSession.checkGameResult();
            } else {
                System.out.println("Error: No active game session found for player " + move.getUsername());
            }
        } catch (Exception e) {
            System.err.println("Error handling challenge move: " + e.getMessage());
            sendParseErrorResponse();
        }
    }

    private void handleClientMessage(String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                sendMessage(PARSE_ERROR, new ParseError());
                return;
            }
            String[] parts = message.split(" ", 2);
            String header = parts[0];
            String body = parts.length > 1 ? parts[1] : "";
            System.out.println("Received message: " + message);
            System.out.println("Header: " + header);
            System.out.println("Body: " + body);
            switch (header) {
                case FILE_UPLOAD_REQ:
                    handleFileUploadRequest(body);
                    break;
                case ENTER:
                    if (body.isEmpty()) {
                        sendMessage(ENTER_RESP, new EnterResp("ERROR", 5001));
                    } else {
                        Enter enter = objectMapper.readValue(body, Enter.class);
                        handleEnter(enter);
                    }
                    break;
                case BROADCAST_REQ:
                    if (!body.isEmpty()) {
                        handleBroadcastRequest(body);
                    } else {
                        sendMessage(PARSE_ERROR, new ParseError());
                    }
                    break;
                case PONG:
                    handlePong();
                    break;
                case PRIVATE_MESSAGE_REQ:
                    if (!body.isEmpty()) {
                        PrivateMessageReq privateMessageReq = objectMapper.readValue(body, PrivateMessageReq.class);
                        handlePrivateMessage(privateMessageReq);
                    } else {
                        sendMessage(PARSE_ERROR, new ParseError());
                    }
                    break;
                case USER_LIST_REQ:
                    if (body.isEmpty() || !isValidJson(body)) {
                        sendMessage(PARSE_ERROR, new ParseError());
                    } else {
                        handleUserListRequest();
                    }
                    break;
                case CHALLENGE_REQ:
                    handleChallengeRequest(body);
                    break;
                case CHALLENGE_RESP:
                    if (!body.isEmpty()) {
                        handleChallengeResponse(body);
                    } else {
                        sendMessage(PARSE_ERROR, new ParseError());
                    }
                    break;
                case FILE_UPLOAD_ACK:
                    handleFileUploadAck(body);
                    break;
                case CHALLENGE_MOVE:
                    handleChallengeMove(body);
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            sendParseErrorResponse();
        }
    }

    private void handleFileUploadAck(String body) throws IOException {
        if (body.isEmpty() || !isValidJson(body)) {
            sendMessage(PARSE_ERROR, new ParseError());
            return;
        }
        FileUploadAck ack = objectMapper.readValue(body, FileUploadAck.class);
        String status = ack.getStatus();
        String originalSender = ack.getFrom();
        ClientHandler originalSenderHandler = getClientByUsername(originalSender);
        if (originalSenderHandler == null) {
            sendMessage(FILE_UPLOAD_INF, new FileUploadInfo(RECIPIENT_NOT_FOUND_FILE_UPLOADING));
            return;
        }
        if ("ACCEPT".equals(status)) {
            originalSenderHandler.sendMessage(FILE_UPLOAD_ACK, new FileUploadInfoConfirm(ACCEPTED));
        } else if ("REJECT".equals(status)) {
            originalSenderHandler.sendMessage(FILE_UPLOAD_ACK, new FileUploadInfoConfirm(REJECTED));
        }
    }

    private void handleChallengeRequest(String body) throws IOException {
        try {
            ChallengeRequest challengeRequest = objectMapper.readValue(body, ChallengeRequest.class);
            String senderUsername = challengeRequest.getUsername();
            String targetUsername = challengeRequest.getTargetUsername();
            ClientHandler targetClient = getClientByUsername(targetUsername);
            if (targetClient == null) {
                sendMessage(CHALLENGE_RESP, new ChallengeResponse(ERROR));
                return;
            }
            sendMessage(CHALLENGE_RESP, new ChallengeResponse(SUCCESS));
            targetClient.sendMessage(CHALLENGE_NOTIFICATION, new ChallengeNotification(senderUsername));
        } catch (Exception e) {
            sendMessage(PARSE_ERROR, new ParseError());
        }
    }

    private void handleChallengeResponse(String body) throws IOException {
        try {
            ChallengeResponse response = objectMapper.readValue(body, ChallengeResponse.class);
            String responseStatus = response.getStatus();
            String targetUser = response.getSender();
            System.out.println("SENDER: " + targetUser + " RECIPIENT: " + response.getReceiver());
            if (targetUser == null || targetUser.isEmpty()) {
                sendMessage(CHALLENGE_RESP, new ChallengeResponse("ERROR", NO_TARGET_SPECIFIED));
                return;
            }
            ClientHandler targetClient = getClientByUsername(targetUser);
            if (targetClient == null) {
                sendMessage(CHALLENGE_RESP, new ChallengeResponse("ERROR", CHALLENGE_USER_NOT_FOUND));
                return;
            }
            if (OK.equals(responseStatus)) {
                sendMessage(CHALLENGE_START, new ChallengeStart());
                targetClient.sendMessage(CHALLENGE_START, new ChallengeStart());
                GameSession gameSession = new GameSession(this, targetClient);
                this.setGameSession(gameSession);
                targetClient.setGameSession(gameSession);
            } else if (REJECT.equals(responseStatus)) {
                targetClient.sendMessage(CHALLENGE_RESP, new ChallengeResponse("REJECT"));
                sendMessage(CHALLENGE_RESP, new ChallengeResponse("REJECT"));
            }
        } catch (Exception e) {
            sendMessage(PARSE_ERROR, new ParseError());
        }
    }

    private void handleFileUploadRequest(String body) throws IOException {
        FileUploadRequest uploadRequest = objectMapper.readValue(body, FileUploadRequest.class);
        String filename = uploadRequest.getFileName();
        String recipientUsername = uploadRequest.getRecipient();
        ClientHandler recipientClient = getClientByUsername(recipientUsername);
        if (recipientClient == null) {
            sendMessage(FILE_UPLOAD_INF, new FileUploadInfo(SENDER_NOT_FOUND_FILE_UPLOADING));
            return;
        }
        recipientClient.sendMessage(FILE_UPLOAD_REQ, new FileUploadInfoReq(filename, getUsername()));
        sendMessage(FILE_UPLOAD_INF, new FileUploadInfoReq());
    }

    private void handleUserListRequest() throws IOException {
        UserListResp userListResp = new UserListResp("OK", connectedUsers);
        System.out.println("Sending USER_LIST_RESP: " + userListResp);
        sendMessage(USER_LIST_RESP, userListResp);
    }

    private void handleBroadcastRequest(String body) throws IOException {
        if (this.username == null) {
            sendMessage(PARSE_ERROR, new ParseError());
            return;
        }
        if (body.isEmpty()) {
            sendMessage(PARSE_ERROR, new ParseError());
            return;
        }
        BroadcastReq broadcastReq = objectMapper.readValue(body, BroadcastReq.class);
        handleBroadcast(broadcastReq);
    }

    private void handlePong() throws IOException {
        Boolean pendingPing = pendingPingMap.getOrDefault(this, false);
        if (pendingPing) {
            pendingPingMap.put(this, false);
            System.out.println("Valid PONG received from " + username);
        } else {
            sendMessage("PONG_ERROR", new PongError(8000));
        }
    }

    private void sendParseErrorResponse() {
        try {
            sendMessage(PARSE_ERROR, new ParseError());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void handleEnter(Enter enter) throws IOException {
        if (enter.username().length() < 3 || enter.username().length() > 14 || enter.username().contains("*")) {
            sendMessage(ENTER_RESP, new EnterResp("ERROR", 5001));
            return;
        }
        synchronized (connectedUsers) {
            if (this.username != null) {
                sendMessage(ENTER_RESP, new EnterResp("ERROR", 5002));
                return;
            }
            if (connectedUsers.contains(enter.username())) {
                sendMessage(ENTER_RESP, new EnterResp("ERROR", 5000));
                return;
            }
            connectedUsers.add(enter.username());
            System.out.println("Users currently connected: " + connectedUsers);
            this.username = enter.username();
            sendMessage(ENTER_RESP, new EnterResp("OK", 5000));
            notifyUsersJoined();
        }
    }

    private void handleBroadcast(BroadcastReq broadcastReq) throws IOException {
        if (username == null || username.isEmpty()) {
            System.out.println("User has not entered, sending PARSE_ERROR.");
            sendMessage(PARSE_ERROR, new ParseError());
            return;
        }
        if (broadcastReq.message().trim().isEmpty()) {
            System.out.println("Received empty broadcast message, sending PARSE_ERROR.");
            sendMessage(PARSE_ERROR, new ParseError());
            return;
        }
        for (ClientHandler client : server.getClients()) {
            if (client != this && client.username != null) {
                client.sendMessage(BROADCAST, new Broadcast(this.username, broadcastReq.message()));
            }
        }
        sendMessage(BROADCAST_RESP, new BroadcastResp("OK", 5000));
    }

    private void handlePrivateMessage(PrivateMessageReq privateMessageReq) throws IOException {
        String recipient = privateMessageReq.recipient();
        String message = privateMessageReq.message();
        if (username == null || username.isEmpty() || recipient == null || message == null) {
            sendMessage(PRIVATE_MESSAGE_RESP, new PrivateMessageResp("ERROR", 7001));
            return;
        }
        ClientHandler recipientHandler = getClientByUsername(recipient);
        if (recipientHandler == null) {
            sendMessage(PRIVATE_MESSAGE_RESP, new PrivateMessageResp("ERROR", 7002));
            return;
        }
        recipientHandler.sendMessage(PRIVATE_MESSAGE, Map.of("from", username, "message", message));
        sendMessage(PRIVATE_MESSAGE_RESP, new PrivateMessageResp("OK"));
    }

    public void sendMessage(String header, Object response) throws IOException {
        String message = header + " " + objectMapper.writeValueAsString(response);
        out.println(message);
    }

    private void notifyUsersJoined() throws IOException {
        for (ClientHandler client : server.getClients()) {
            if (client != this && client.username != null) {
                client.sendMessage(JOINED, new Joined(this.username));
            }
        }
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void disconnectClient() {
        try {
            if (fileSocket != null) fileSocket.close();
            if (username != null) {
                connectedUsers.remove(username);
            }
            server.getClients().remove(this);
            pendingPingMap.remove(this);
            if (!socket.isClosed()) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}