package Client.language;
import java.util.*;

public class CLIClientConsoleLogger implements ClientLogger {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void info(String message) {
        System.out.println(message);

    }

    @Override
    public void infoLine(String message) {
        System.out.print(message);
    }

    @Override
    public void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    @Override
    public String getInputWithString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    @Override
    public String getInput() {
        return scanner.nextLine();
    }
}