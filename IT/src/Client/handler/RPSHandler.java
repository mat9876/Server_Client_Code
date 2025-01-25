package Client.handler;
import Client.*;
import Client.language.*;
import Client.messages.*;
import Client.interfaces.ClientHeaderCommands;

import java.io.PrintWriter;

public class RPSHandler implements ClientHeaderCommands {
    private final CLIClientConsoleLogger consoleLogger;
    private final LanguageManager languageManager;
    private final UsernameClient mainApp;

    public RPSHandler(CLIClientConsoleLogger consoleLogger, LanguageManager languageManager, UsernameClient mainApp) {
        this.consoleLogger = consoleLogger;
        this.languageManager = languageManager;
        this.mainApp = mainApp;
    }

    public void handleRPSInput(String choice, PrintWriter out) {
        String move;
        switch (choice) {
            case "1":
                move = ROCK;
                break;
            case "2":
                move = PAPER;
                break;
            case "3":
                move = SCISSORS;
                break;
            default:
                consoleLogger.info(languageManager.getMessage("InvalidChoice"));
                return;
        }
        RPSMove rpsMove = new RPSMove(mainApp.getUsername(), move);
        mainApp.sendMessage(out, CHALLENGE_MOVE, rpsMove);
        consoleLogger.info(languageManager.getMessage("MoveSent") + move);
        mainApp.setInRPSChallenge(false); // Exit RPS mode after making a move.
    }
}
