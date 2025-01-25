package server.dataClasses;
import server.handler.*;
import server.interfaces.*;
import server.messages.*;
import java.io.*;

public class GameSession implements ServerHeaderCommands {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private RPSMove player1Move;
    private RPSMove player2Move;

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void storeMove(ClientHandler player, RPSMove move) {
        if (player == player1) {
            player1Move = move;
            System.out.println("Player 1 (" + player.getUsername() + ") made move: " + move.getMove());
        } else if (player == player2) {
            player2Move = move;
            System.out.println("Player 2 (" + player.getUsername() + ") made move: " + move.getMove());
        } else {
            System.out.println("Error: Player not found in the session!");
        }
    }

    public void checkGameResult() throws IOException {
        if (player1Move != null && player2Move != null) {
            System.out.println("Both players have made their moves:");
            System.out.println("Player 1 (" + player1.getUsername() + "): " + player1Move.getMove());
            System.out.println("Player 2 (" + player2.getUsername() + "): " + player2Move.getMove());
            String winner = determineWinner(player1Move, player2Move);
            System.out.println("Winner: " + winner);
            ChallengeNotification winnerNotification = new ChallengeNotification(winner);
            player1.sendMessage(CHALLENGE_COMPLETE_NOTIFICATION, winnerNotification);
            player2.sendMessage(CHALLENGE_COMPLETE_NOTIFICATION, winnerNotification);
        } else {
            System.out.println("Waiting for both players to make their moves...");
        }
    }

    private String determineWinner(RPSMove move1, RPSMove move2) {
        if (move1.getMove().equals(move2.getMove())) {
            return "DRAW";
        }
        switch (move1.getMove()) {
            case "ROCK":
                return move2.getMove().equals("SCISSORS") ? getPlayer1().getUsername() : getPlayer2().getUsername();
            case "PAPER":
                return move2.getMove().equals("ROCK") ? getPlayer1().getUsername() : getPlayer2().getUsername();
            case "SCISSORS":
                return move2.getMove().equals("PAPER") ? getPlayer1().getUsername() : getPlayer2().getUsername();
            default:
                return "ERROR";
        }
    }
}