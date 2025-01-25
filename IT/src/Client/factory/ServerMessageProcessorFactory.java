package Client.factory;
import Client.*;
import Client.messages.*;
import Client.records.dataClasses.*;
import Client.interfaces.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerMessageProcessorFactory implements FileClientHeaderCommands, ClientHeaderCommands {
    private final UsernameClient usernameClient;

    public UsernameClient getUsernameClient() {
        return usernameClient;
    }

    public ServerMessageProcessorFactory(UsernameClient usernameClient) {
        this.usernameClient = usernameClient;
    }

    protected void interruptThreads() {
        getUsernameClient().getServerListener().interrupt();
        getUsernameClient().getInputHandler().interrupt();
    }

    protected void ProcessorHandleJoined(String serverMessage) {
        try {
            Map<String, String> joinedMessage = getUsernameClient().getObjectMapper().readValue(serverMessage.substring("JOINED ".length()), new TypeReference<>() {});
            getUsernameClient().getConsoleLogger().info(joinedMessage.get("username") + getUsernameClient().getLanguageManager().getMessage("HandleReceivingJoined"));
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error(getUsernameClient().getLanguageManager().getMessage("HandleJoinedError") + serverMessage);
        }
    }

    protected void handleBroadcast(String serverMessage) {
        try {
            Map<String, String> broadcastMessage = getUsernameClient().getObjectMapper().readValue(serverMessage.substring("BROADCAST ".length()), new TypeReference<>() {});
            getUsernameClient().getConsoleLogger().info(getUsernameClient().getLanguageManager().getMessage("HandleReceivingBroadcast") + broadcastMessage.get("username") + ": " + broadcastMessage.get("message"));
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error(getUsernameClient().getLanguageManager().getMessage("HandleBroadcastError") + serverMessage);
        }
    }

    protected void handleChallengeNotification(String serverMessage) {
        try {
            getUsernameClient().setSender(getUsernameClient().getObjectMapper().readValue(serverMessage.substring("CHALLENGE_NOTIFICATION ".length()), ChallengeNotification.class).senderUsername());
            getUsernameClient().getConsoleLogger().info(getUsernameClient().getLanguageManager().getMessage("ChallengeReceived") + getUsernameClient().getSender());
            getUsernameClient().setInChallenge(true);
            getUsernameClient().getShowMenu().showChallengeMenu();
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error(getUsernameClient().getLanguageManager().getMessage("ErrorParsingChallengeNotification") + serverMessage
            );
        }
    }

    private void handlePrivateMessageResponse(String serverMessage) {
        try {
            PrivateMessageResp response = parsePrivateMessageResp(serverMessage);
            if ("OK".equalsIgnoreCase(response.getStatus())) {
                getUsernameClient().getConsoleLogger().info(getUsernameClient().getLanguageManager().getMessage("PrivateMessageSent"));
            } else {
                String errorMessage = switch (response.getErrorCode()) {
                    case 7001 -> "Invalid private message payload.";
                    case 7002 -> "Recipient not found.";
                    default -> "Unknown error.";
                };
                getUsernameClient().getConsoleLogger().error("Failed to send private message: " + errorMessage);
            }
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error("Failed to process private message response: " + serverMessage + ": " + e);
        }
    }

    private void handlePrivateMessage(String serverMessage) {
        try {
            Map<String, String> messageData = parseServerMessage(serverMessage);
            String message = messageData.get("message");
            getUsernameClient().getConsoleLogger().info("Private message from: " + message);
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error("Failed to process private message: " + serverMessage + ": "+ e);
        }
    }

    private Map<String, String> parseServerMessage(String serverMessage) throws IOException {
        String json = serverMessage.substring(serverMessage.indexOf(' ') + 1);
        return new ObjectMapper().readValue(json, new TypeReference<>() {
        });
    }

    private PrivateMessageResp parsePrivateMessageResp(String serverMessage) throws IOException {
        String json = serverMessage.substring(serverMessage.indexOf(' ') + 1);
        return new ObjectMapper().readValue(json, PrivateMessageResp.class);
    }

    private void handleChallengeResponse(String serverMessage) {
        try {
            String json = serverMessage.substring(serverMessage.indexOf(' ') + 1);
            ChallengeResponse response = new ObjectMapper().readValue(json, ChallengeResponse.class);
            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                getUsernameClient().getConsoleLogger().info("Challenge request successful!");
            } else {
                getUsernameClient().getConsoleLogger().error("Challenge request failed.");
            }
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error("Failed to process challenge response: " + serverMessage + ": "+ e);
        }
    }

    private void handleChallengeCompleteNotification(String serverMessage) {
        try {
            String json = serverMessage.substring(serverMessage.indexOf(' ') + 1);
            ChallengeNotification notification = new ObjectMapper().readValue(json, ChallengeNotification.class);
            String result = notification.senderUsername();
            if ("DRAW".equalsIgnoreCase(result)) {
                getUsernameClient().getConsoleLogger().info("The game ended in a draw!");
            } else {
                getUsernameClient().getConsoleLogger().info("The winner is: " + result);
            }
        } catch (Exception e) {
            getUsernameClient().getConsoleLogger().error("Failed to process challenge complete notification: " + serverMessage + ": "+ e);
        }
    }

    public void uploadFileToServer(String filePath) throws IOException {
        String sender = getUsernameClient().getFileSender();
        OutputStream outputStream = null;
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                getUsernameClient().getConsoleLogger().error("File does not exist or is not a valid file: " + filePath);
                return;
            }
            System.out.println("A:" + file.length());
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                outputStream = getUsernameClient().getFileSocket().getOutputStream();
                getUsernameClient().getConsoleLogger().info("Starting file upload: " + file.getName());
                String metadata = String.format("FILE_META %s %d %s\n", file.getName(), file.length(), sender);
                outputStream.write(metadata.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                byte[] buffer = new byte[getUsernameClient().getMAX_BYTES()];
                int bytesRead;
                int totalByteRead = 0;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalByteRead += bytesRead;
                    System.out.println("Q: "+ totalByteRead);
                    outputStream.flush();
                }
                getUsernameClient().getConsoleLogger().info("File upload completed: " + file.getName());
            }
        } catch (IOException e) {
            getUsernameClient().getConsoleLogger().error("Error during file upload: " + e.getMessage());
        } finally {
            outputStream.close();
            getUsernameClient().getFileSocket().close();
        }
    }


    public void processServerMessage(String serverMessage, PrintWriter out) throws IOException {
        if (serverMessage.startsWith(FILE_UPLOAD_REQ)) {
            String body = serverMessage.substring(FILE_UPLOAD_REQ.length()).trim();
            FileUploadInfoReq uploadInfo = getUsernameClient().getObjectMapper().readValue(body, FileUploadInfoReq.class);
            getUsernameClient().getConsoleLogger().info("File upload request from " + uploadInfo.getUserName() + " for file " + uploadInfo.getFileName());
            getUsernameClient().setInFileUploadRequest(true);
            getUsernameClient().getShowMenu().showFileUploadMenu();
        } else if (serverMessage.startsWith(FILE_UPLOAD_INF)) {
            System.out.println(serverMessage);
        } else if (serverMessage.startsWith(FILE_UPLOAD_ACK)){
            String body = serverMessage.substring(FILE_UPLOAD_INF.length()).trim();
            Map<String, Object> jsonResponse = getUsernameClient().getObjectMapper().readValue(body, new TypeReference<Map<String, Object>>() {});
            int status = (int) jsonResponse.get("status");
            if (status == ACCEPTED) {
                System.out.println("File upload request accepted.");
                String absoluteFilePath = getUsernameClient().getPendingUploadFilePath();
                uploadFileToServer(absoluteFilePath);
            } else if (status == REJECTED) {
                getUsernameClient().getConsoleLogger().info("File upload request rejected.");
            }
        } else if (serverMessage.startsWith(PRIVATE_MESSAGE)) {
            handlePrivateMessage(serverMessage);
        } else if (serverMessage.startsWith(PRIVATE_MESSAGE_RESP)) {
            handlePrivateMessageResponse(serverMessage);
        } else if (serverMessage.startsWith(PING)) {
            getUsernameClient().sendMessage(out, PONG, new Pong());
        } else if(serverMessage.startsWith(PONG_ERROR)){
            interruptThreads();
        } else if(serverMessage.startsWith(BROADCAST_RESP)) {
            getUsernameClient().getConsoleLogger().info(getUsernameClient().getLanguageManager().getMessage("HandleConformationSendingBroadcast"));
        } else if (serverMessage.startsWith(BROADCAST)) {
            handleBroadcast(serverMessage);
        } else if (serverMessage.startsWith(JOINED)) {
            ProcessorHandleJoined(serverMessage);
        } else if (serverMessage.startsWith(PARSE_ERROR)){
            getUsernameClient().getConsoleLogger().info(serverMessage);
        } else if (serverMessage.startsWith(CHALLENGE_RESP)) {
            handleChallengeResponse(serverMessage);
        } else if (serverMessage.startsWith(CHALLENGE_NOTIFICATION)) {
            handleChallengeNotification(serverMessage);
        } else if (serverMessage.startsWith(CHALLENGE_START)) {
            getUsernameClient().setInRPSChallenge(true);
            getUsernameClient().getShowMenu().showRPSMenu();
        } else if (serverMessage.startsWith(CHALLENGE_COMPLETE_NOTIFICATION)) {
            handleChallengeCompleteNotification(serverMessage);
        } else if(serverMessage.startsWith(FILE_UPLOAD_NOTIFICATION)) {
            System.out.println(serverMessage);
        }  else {
            getUsernameClient().getConsoleLogger().error(getUsernameClient().getLanguageManager().getMessage("InvalidMessage") + serverMessage);
        }
    }
}