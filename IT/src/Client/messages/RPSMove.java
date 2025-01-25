package Client.messages;

public class RPSMove {
    private String username;
    private String move;

    public RPSMove(String username, String move) {
        this.username = username;
        this.move = move;
    }

    public String getUsername() {
        return username;
    }

    public String getMove() {
        return move;
    }
}