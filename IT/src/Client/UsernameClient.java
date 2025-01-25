package Client;
import Client.factory.*;
import Client.handler.*;
import Client.language.*;
import Client.messages.*;
import Client.records.*;
import Client.records.dataClasses.*;
import Client.interfaces.ClientHeaderCommands;
import Client.interfaces.FileClientHeaderCommands;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UsernameClient implements FileClientHeaderCommands, ClientHeaderCommands {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1337;
    private static final int FILE_PORT = 1338;
    private int MAX_BYTES = 8192;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;
    private final Socket fileSocket;
    private static String username;
    private String pendingUploadFilePath;
    private String sender;
    private String fileSender;
    private volatile boolean running = true;
    private boolean isInChallenge = false;
    private boolean isInRPSChallenge = false;
    private boolean isInFileUploadRequest = false;
    private final LanguageManager languageManager;
    private final CLIClientConsoleLogger consoleLogger;
    private ShowMenu showMenu;
    private ServerMessageProcessorFactory serverMessageProcessorFactory;
    private Thread serverListener;
    private Thread inputHandler;
    private MainMenuHandler mainMenuHandler;
    private ChallengeMenuHandler challengeMenuHandler;
    private RPSHandler rpsHandler;
    private FileUploadHandler fileUploadHandler;
    private FileOutputStream fileOutputStream;

    public String getFileSender() {
        return fileSender;
    }

    public void setFileSender(String fileSender) {
        this.fileSender = fileSender;
    }

    public int getMAX_BYTES() {
        return MAX_BYTES;
    }

    public FileUploadHandler getFileUploadHandler() {
        return fileUploadHandler;
    }

    public RPSHandler getRpsHandler() {
        return rpsHandler;
    }

    public ChallengeMenuHandler getChallengeMenuHandler() {
        return challengeMenuHandler;
    }

    public MainMenuHandler getMainMenuHandler() {
        return mainMenuHandler;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Thread getServerListener() {
        return serverListener;
    }

    public void setServerMessageProcessor(ServerMessageProcessorFactory serverMessageProcessorFactory) {
        this.serverMessageProcessorFactory = serverMessageProcessorFactory;
    }

    public ServerMessageProcessorFactory getServerMessageProcessor() {
        return serverMessageProcessorFactory;
    }

    public void setServerListener(Thread serverListener) {
        this.serverListener = serverListener;
    }

    public Thread getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(Thread inputHandler) {
        this.inputHandler = inputHandler;
    }

    public Socket getSocket() {
        return socket;
    }

    public ShowMenu getShowMenu() {
        return showMenu;
    }

    public void setShowMenu(ShowMenu showMenu) {
        this.showMenu = showMenu;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Socket getFileSocket() {
        return fileSocket;
    }

    public String getPendingUploadFilePath() {
        return pendingUploadFilePath;
    }

    public void setPendingUploadFilePath(String pendingUploadFilePath) {
        this.pendingUploadFilePath = pendingUploadFilePath;
    }

    public String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UsernameClient.username = username;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isInRPSChallenge() {
        return isInRPSChallenge;
    }

    public void setInRPSChallenge(boolean inRPSChallenge) {
        isInRPSChallenge = inRPSChallenge;
    }

    public boolean isInFileUploadRequest() {
        return isInFileUploadRequest;
    }

    public void setInFileUploadRequest(boolean inFileUploadRequest) {
        isInFileUploadRequest = inFileUploadRequest;
    }

    public boolean isInChallenge() {
        return isInChallenge;
    }

    public void setInChallenge(boolean inChallenge) {
        isInChallenge = inChallenge;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public CLIClientConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }

    public static void main(String[] args) {
     UsernameClient usernameClient = new UsernameClient();
     usernameClient.run();
    }

    public UsernameClient() {
        this.consoleLogger = new CLIClientConsoleLogger(); //NOTE: Change logger here.
        this.languageManager = new LanguageManager(this);
        this.mainMenuHandler = new MainMenuHandler(consoleLogger, languageManager, this);
        this.challengeMenuHandler = new ChallengeMenuHandler(consoleLogger, languageManager, this);
        this.rpsHandler = new RPSHandler(consoleLogger, languageManager, this);
        this.fileUploadHandler = new FileUploadHandler(consoleLogger, languageManager, this);
        try {
            this.fileSocket = new Socket(SERVER_ADDRESS, FILE_PORT);
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            getConsoleLogger().info("File socket initialized successfully: " + fileSocket);
        } catch (IOException e) {
            getConsoleLogger().error("Failed to initialize file socket: " + e.getMessage() +": "+ e);
            throw new RuntimeException(e);
        }
    }

    public void run() {
        getConsoleLogger().info(getLanguageManager().getMessage("ConnectingToServer"));
        try {
            getConsoleLogger().info(getLanguageManager().getMessage("ConnectToServer"));
            initializeFileReceiver();
            processServerConnection();
        } catch (IOException | InterruptedException e) {
            getConsoleLogger().error(e.getMessage());
        } finally {
           finalizeFileOutput();
           stop();
        }
    }

    private void initializeFileReceiver() {
        Thread fileReceiverThread = new Thread(() -> {
            try (InputStream fileInputStream = getFileSocket().getInputStream()) {
                byte[] buffer = new byte[getMAX_BYTES()];
                while (isRunning()) {
                    int bytesRead = fileInputStream.read(buffer);
                    if (bytesRead == -1) break;
                    System.out.println("Y: " + bytesRead);
                    processFileTransferData(buffer, bytesRead);
                }
            } catch (IOException e) {
                consoleLogger.error("Error in file receiver: " + e.getMessage());
            }
        });
        fileReceiverThread.start();
    }

    private void processFileTransferData(byte[] data, int length) {
        String message = new String(data, 0, length, StandardCharsets.UTF_8);
        if (message.startsWith("FILE_META")) {
            System.out.println("X: " + message);
            String[] parts = message.split(" ", 4);
            String fileName = parts[1];
            long fileSize = Long.parseLong(parts[2]);
            consoleLogger.info("Receiving file: " + fileName + " (" + fileSize + " bytes)");
            prepareFileOutput(fileName);
        } else if (length > 0) {
            writeFileChunk(data, length);
        }
    }

    private void finalizeFileOutput() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            consoleLogger.error("Error closing file output stream: " + e.getMessage());
        }
    }

    private void prepareFileOutput(String fileName) {
        try {
            File file = new File("src/Client/uploads/" + fileName);
            file.getParentFile().mkdirs(); // Ensure directory exists
            fileOutputStream = new FileOutputStream(file);
        } catch (IOException e) {
            consoleLogger.error("Failed to prepare file output: " + e.getMessage());
        }
    }

    private void writeFileChunk(byte[] data, int length) {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.write(data, 0, length);
                fileOutputStream.flush();
            }
        } catch (IOException e) {
            consoleLogger.error("Error writing file chunk: " + e.getMessage());
        }
    }

    private void processServerConnection() throws IOException, InterruptedException {
        try (PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()))) {
            handleServerReadyMessage(in);
            authenticateUser(out, in);
            initializeThreads(out, in);
        }
    }

    private void handleServerReadyMessage(BufferedReader in) throws IOException {
        String readyMessage = in.readLine();
        getConsoleLogger().info(getLanguageManager().getMessage("Server") + ": " + readyMessage);
    }

    private void authenticateUser(PrintWriter out, BufferedReader in) throws IOException {
        setUsername(getConsoleLogger().getInputWithString(getLanguageManager().getMessage("LoginMessage")));
        sendMessage(out, ENTER, new Enter(getUsername()));
        String enterResponse = in.readLine();
        getConsoleLogger().info(getLanguageManager().getMessage("Server") + ": " + enterResponse);
        if (!isResponseOk(enterResponse)) {
            getConsoleLogger().info(getLanguageManager().getMessage("FailedLogin"));
            throw new IOException("Authentication failed.");
        }
        setShowMenu(new ShowMenu(this));
        setServerMessageProcessor(new ServerMessageProcessorFactory(this));
    }

    private void initializeThreads(PrintWriter out, BufferedReader in) throws InterruptedException {
        setServerListener(new Thread(() -> listenToServer(in, out)));
        setInputHandler(new Thread(() -> handleUserInputLoop(out)));
        getServerListener().start();
        getInputHandler().start();
        getInputHandler().join();
        getServerListener().interrupt();
    }

    private void listenToServer(BufferedReader in, PrintWriter out) {
        try {
            while (isRunning()) {
                if (in.ready()) {
                    String serverMessage = in.readLine();
                    if (serverMessage != null && !serverMessage.isEmpty()) {
                        getConsoleLogger().info("\n" + getLanguageManager().getMessage("Server") + ": " + serverMessage);
                        getServerMessageProcessor().processServerMessage(serverMessage, out);
                    }
                }
            }
        } catch (IOException e) {
            if (isRunning()) {
                getConsoleLogger().error(getLanguageManager().getMessage("ErrorServerReading") + e.getMessage());
            }
        }
    }

    public void handleFileAccept(PrintWriter out) {
        sendMessage(out, FILE_UPLOAD_ACK, new FileUploadAck(ACCEPT, getSender()));
        setInFileUploadRequest(false); // Reset the state
        getConsoleLogger().info("File upload request accepted.");
    }

    public void handleFileReject(PrintWriter out) {
        sendMessage(out, FILE_UPLOAD_ACK, new FileUploadAck(REJECT, getSender()));
        setInFileUploadRequest(false); // Reset the state
        getConsoleLogger().info("File upload request rejected.");
    }

    private void handleUserInputLoop(PrintWriter out) {
        while (isRunning()) {
            if (isInFileUploadRequest()) {
                getShowMenu().showFileUploadMenu();
            } else if (!isInChallenge() && !isInRPSChallenge()) {
                getShowMenu().showMenu();
            } else if (isInChallenge()) {
                getShowMenu().showChallengeMenu();
            } else if (isInRPSChallenge()) {
                getShowMenu().showRPSMenu();
            }
            String choice = getConsoleLogger().getInput();
            handleUserInput(choice, out);
        }
    }

    private void handleUserInput(String choice, PrintWriter out) {
        try {
            if (isInFileUploadRequest()) {
                getFileUploadHandler().handleFileUploadInput(choice, out);
            } else if (!isInChallenge() && !isInRPSChallenge()) {
                getMainMenuHandler().handleMainMenuInput(choice, out);
            } else if (isInChallenge()) {
                getChallengeMenuHandler().handleChallengeMenuInput(choice, out);
            } else if (isInRPSChallenge()) {
                getRpsHandler().handleRPSInput(choice, out);
            }
        } catch (IOException e) {
            getConsoleLogger().error(getLanguageManager().getMessage("ErrorServerSending") + e.getMessage());
        }
    }

    public void handleBroadcast(PrintWriter out) {
        String message = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterBroadcast"));
        sendMessage(out, BROADCAST_REQ, new BroadcastReq(message));
    }

    public void handlePrivateMessage(PrintWriter out) {
        String recipient = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterPrivateRecipient"));
        String message = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterPrivateMessage"));
        sendMessage(out, PRIVATE_MESSAGE_REQ, new PrivateMessageReq(recipient, message));
    }

    public void handleChallengeUser(PrintWriter out) {
        String targetUsername = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterRecipientChallenge"));
        sendMessage(out, CHALLENGE_REQ, new ChallengeRequest(targetUsername, getUsername()));
    }

    public void handleAcceptChallenge(PrintWriter out) {
        sendMessage(out, CHALLENGE_RESP, new ChallengeResponse(OK, getSender(), getUsername()));
        setInChallenge(false);
    }

    public void handleRejectChallenge(PrintWriter out) {
        sendMessage(out, CHALLENGE_RESP, new ChallengeResponse(REJECT, getSender(), getUsername()));
        setInChallenge(false);
    }

    public void sendMessage(PrintWriter out, String header, Object message) {
        try {
            String jsonMessage = getObjectMapper().writeValueAsString(message);
            out.println(header.trim() + " " + jsonMessage);
            getConsoleLogger().info(getLanguageManager().getMessage("ClientSent") + header.trim() + " " + jsonMessage);
        } catch (IOException e) {
            getConsoleLogger().error(e.getMessage());
        }
    }

    public void handleFileUploadOption(PrintWriter out) {
        String recipientUsername = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterPrivateRecipient"));
        String filename = getConsoleLogger().getInputWithString(getLanguageManager().getMessage("EnterFilePath"));
        setPendingUploadFilePath(filename);
        setFileSender(recipientUsername);
        requestFileUpload(filename, recipientUsername, out);
    }

    public void requestFileUpload(String fullPath, String recipientUsername, PrintWriter out) {
        String filename = new File(fullPath).getName();
        FileUploadRequest uploadRequest = new FileUploadRequest(filename, recipientUsername, getUsername());
        sendMessage(out, FILE_UPLOAD_REQ, uploadRequest);
        getConsoleLogger().info(getLanguageManager().getMessage("FileUploadRequested") + " to " + recipientUsername);
    }

    private boolean isResponseOk(String response) {
        try {
            EnterResp enterResp = getObjectMapper().readValue(response.substring(response.indexOf(" ") + 1), EnterResp.class);
            return OK.equals(enterResp.status());
        } catch (Exception e) {
            getConsoleLogger().error(e.getMessage());
            return false;
        }
    }

    public void stop() {
        setRunning(false);
        try {
            if (getFileSocket() != null && !getFileSocket().isClosed()) {
                getFileSocket().close();
            }
            if (getSocket() != null && !getSocket().isClosed()) {
                getSocket().close();
            }
        } catch (IOException e) {
            getConsoleLogger().error("Error closing sockets: " + e.getMessage());
        }
    }
}