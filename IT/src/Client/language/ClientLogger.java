package Client.language;

public interface ClientLogger {
    void info(String message);
    void infoLine(String message);
    void error(String message);
    String getInputWithString(String prompt);
    String getInput();
}
