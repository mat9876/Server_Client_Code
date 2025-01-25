package Client.handler;
import Client.*;
import Client.language.*;
import Client.messages.*;
import Client.interfaces.ClientHeaderCommands;

import java.io.*;

public class MainMenuHandler implements ClientHeaderCommands {
    private final CLIClientConsoleLogger consoleLogger;
    private final LanguageManager languageManager;
    private final UsernameClient mainApp;

    public MainMenuHandler(CLIClientConsoleLogger consoleLogger, LanguageManager languageManager, UsernameClient mainApp) {
        this.consoleLogger = consoleLogger;
        this.languageManager = languageManager;
        this.mainApp = mainApp;
    }

    public void handleMainMenuInput(String choice, PrintWriter out) throws IOException {
        switch (choice) {
            case "1":
                mainApp.handleBroadcast(out);
                break;
            case "2":
                mainApp.handlePrivateMessage(out);
                break;
            case "3":
                mainApp.sendMessage(out, USER_LIST_REQ, new UserListReq());
                break;
            case "4":
                mainApp.handleChallengeUser(out);
                break;
            case "5":
                mainApp.handleFileUploadOption(out);
                break;
            case "6":
                languageManager.displayLanguageMenu();
                break;
            case "7":
                consoleLogger.info(languageManager.getMessage("Exiting"));
                mainApp.setRunning(false);
                break;
            default:
                consoleLogger.info(languageManager.getMessage("InvalidChoice"));
                break;
        }
    }
}
