package Client.handler;
import Client.*;
import Client.language.*;
import java.io.*;

public class ChallengeMenuHandler {
    private final CLIClientConsoleLogger consoleLogger;
    private final LanguageManager languageManager;
    private final UsernameClient mainApp;

    public ChallengeMenuHandler(CLIClientConsoleLogger consoleLogger, LanguageManager languageManager, UsernameClient mainApp) {
        this.consoleLogger = consoleLogger;
        this.languageManager = languageManager;
        this.mainApp = mainApp;
    }

    public void handleChallengeMenuInput(String choice, PrintWriter out) throws IOException {
        switch (choice) {
            case "1":
                mainApp.handleAcceptChallenge(out);
                break;
            case "2":
                mainApp.handleRejectChallenge(out);
                break;
            default:
                consoleLogger.info(languageManager.getMessage("InvalidChoice"));
                break;
        }
    }
}
