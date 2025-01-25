package Client.handler;
import Client.*;
import Client.language.*;
import java.io.*;

public class FileUploadHandler {
    private final CLIClientConsoleLogger consoleLogger;
    private final LanguageManager languageManager;
    private final UsernameClient usernameClient;

    public FileUploadHandler(CLIClientConsoleLogger consoleLogger, LanguageManager languageManager, UsernameClient usernameClient) {
        this.consoleLogger = consoleLogger;
        this.languageManager = languageManager;
        this.usernameClient = usernameClient;
    }

    public void handleFileUploadInput(String choice, PrintWriter out) throws IOException {
        switch (choice) {
            case "1":
                usernameClient.handleFileAccept(out);
                break;
            case "2":
                usernameClient.handleFileReject(out);
                break;
            default:
                consoleLogger.info(languageManager.getMessage("InvalidChoice"));
                break;
        }
    }
}
