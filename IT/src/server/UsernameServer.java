package server;
import server.handler.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class UsernameServer {
    private static final int PORT = 1337;
    private static final int FILE_PORT = 1338;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private Socket clientSocket;
    private Socket fileSocket;

    public static void main(String[] args) {
        new UsernameServer().run();
    }

    public void run() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT); ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
            System.out.println("Server is running on port " + PORT);
            System.out.println("File server is running on port " + FILE_PORT);
            threadPool.submit(() -> handleFileReceiving(fileServerSocket));
            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("New client connection accepted.");
                    ClientHandler clientHandler = new ClientHandler(clientSocket, fileSocket, this);
                    clients.add(clientHandler);
                    threadPool.submit(clientHandler);
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void handleFileReceiving(ServerSocket fileServerSocket) {
        while (true) {
            try {
                fileSocket = fileServerSocket.accept();
                System.out.println("New file upload connection accepted.");
                new Thread(new FileReceiver(fileSocket, this)).start();
            } catch (IOException e) {
                System.err.println("Error accepting file connection: " + e.getMessage());
            }
        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void sendFileToClient(String username, byte[] rawBytes, String fileName, long fileSize) throws IOException {
        System.out.println("[DEBUG] Sending file to client: " + username);
        ClientHandler client = getClientByUsername(username);
        if (client == null) {
            System.err.println("[ERROR] No client found with username: " + username);
            return;
        }
        OutputStream clientOut = null;
        PrintWriter writer = null;
        try {
            clientOut = client.getFileSocket().getOutputStream();
            writer = new PrintWriter(clientOut, true);
            String metadata = "FILE_META " + fileName + " " + fileSize;
            System.out.println("[DEBUG] Sending metadata: " + metadata);
            writer.println(metadata);
            writer.flush();
            clientOut.write(rawBytes);
            clientOut.flush();
            System.out.println("[DEBUG] File sent successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Error sending file to client: " + e.getMessage());
        } finally {
            writer.close();
            clientOut.close();
        }
    }

    public ClientHandler getClientByUsername(String username) {
        for (ClientHandler client : getClients()) {
            if (username.equals(client.getUsername())) {
                return client;
            }
        }
        return null;
    }

    /** Inner class to handle file receiving. **/
    private static class FileReceiver implements Runnable {
        private final Socket fileSocket;
        private final UsernameServer server;
        private File outputFile;

        public FileReceiver(Socket fileSocket, UsernameServer server) {
            this.fileSocket = fileSocket;
            this.server = server;
        }

        @Override
        public void run() {

            try (InputStream inputStream = fileSocket.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    if (ch == '\n') {
                        break;
                    }
                    sb.append((char) ch);
                }
                String metadata = sb.toString();
                System.out.println("Q:" + metadata);
                if (metadata.isEmpty() || !metadata.startsWith("FILE_META")) {
                    System.err.println("Invalid file metadata received.");
                    return;
                }
                String[] metaParts = metadata.split(" ", 4);
                if (metaParts.length < 4) {
                    System.err.println("Invalid file metadata format.");
                    return;
                }
                String fileName = metaParts[1];
                long fileSize = Long.parseLong(metaParts[2]);
                String sender = metaParts[3];
                File uploadDir = new File("IT/src/server/uploads/");
                if (!uploadDir.exists()) {
                    if (uploadDir.mkdirs()) {
                        System.out.println("Uploads directory created at: " + uploadDir.getAbsolutePath());
                    } else {
                        System.err.println("Failed to create uploads directory at: " + uploadDir.getAbsolutePath());
                        return;
                    }
                }
                outputFile = new File(uploadDir, fileName);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(outputFile); //NOTE: BYTES HERE.
                    byte[] buffer = new byte[8192];
                    System.out.println("X:" + outputFile.length());
                    int bytesRead;
                    long totalBytesRead = 0;
                    System.out.println("Y:" + fileSize);
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        fileOutputStream.flush();
                        totalBytesRead += bytesRead;
                        if (totalBytesRead >= fileSize) {
                            break;
                        }
                        byteArrayOutputStream.write(buffer, 0, bytesRead); //Buffer the raw bytes
                        totalBytesRead += bytesRead;
                    }
                    System.out.println("C:" + totalBytesRead + " D:" + outputFile.length() + " E:" + fileSize);
                    System.out.println("File received and saved: " + outputFile.getAbsolutePath());
                    if (totalBytesRead != fileSize) {
                        System.err.println("[ERROR] File size mismatch. Expected: " + fileSize + ", Received: " + totalBytesRead);
                    } else {
                        System.out.println("[DEBUG] File received and saved: " + outputFile.getAbsolutePath());
                        server.sendFileToClient(sender, byteArrayOutputStream.toByteArray(), fileName, fileSize);
                    }
                } finally {
                    assert fileOutputStream != null;
                    fileOutputStream.close();
                    fileSocket.close();
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error receiving file: " + e.getMessage());
            } finally {
                try {
                    fileSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing file socket: " + e.getMessage());
                }
            }
        }
    }
}