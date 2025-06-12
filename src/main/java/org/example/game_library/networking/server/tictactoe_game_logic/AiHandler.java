package org.example.game_library.networking.server.tictactoe_game_logic;

import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.TicTacToeRepository;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AiHandler implements MoveHandler {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            int row = Integer.parseInt(request.get(2));
            int col = Integer.parseInt(request.get(3));
            String symbol = "X";
            String aiSymbol = "O";

            TicTacToeGame game = threadCreator.getTicTacToeGame();

            if (!symbol.equals(game.getCurrentSymbol())) {
                output.writeObject("FAILURE: Not your turn!");
                return;
            }

            if (!game.makeMove(row, col, symbol)) {
                output.writeObject("FAILURE: Move failed! Cell already occupied!");
                return;
            }

            if (handleEndGame(game, "WIN: " + symbol, output, threadCreator)) return;

            game.togglePlayer();

            int[] aiMove = makeAIMove(game, aiSymbol);
            if (aiMove == null) {
                handleEndGame(game, "DRAW!", output, threadCreator);
                return;
            }

            output.writeObject("AI_MOVE:" + aiMove[0] + "," + aiMove[1]);

            if (handleEndGame(game, "LOSE: " + aiSymbol, output, threadCreator)) return;

            game.setCurrentSymbol("X");
            output.writeObject("SUCCESS");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while processing move: {0}", e.getMessage());
        }
    }

    private int[] makeAIMove(TicTacToeGame game, String aiSymbol) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (game.getBoard()[i][j].equals(" ")) {
                    game.getBoard()[i][j] = aiSymbol;

                    int score = minimax(game, 0, false, aiSymbol);
                    game.getBoard()[i][j] = " ";

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{i, j};
                    }
                }
            }
        }

        if (bestMove != null) {
            game.makeMove(bestMove[0], bestMove[1], aiSymbol);
        }

        return bestMove;
    }

    private boolean handleEndGame(TicTacToeGame game, String resultMessage, ObjectOutputStream output, ThreadCreator threadCreator) {
        try {
            if (game.checkWin()) {
                if (resultMessage.startsWith("WIN:")) {
                    User currentUser = threadCreator.getCurrentUser();
                    if (currentUser != null) {
                        try {
                            TicTacToeRepository.incrementWins(currentUser, "ai");
                        } catch (Exception e) {
                            System.err.println("Could not update score: " + e.getMessage());
                        }
                    }
                }
                output.writeObject(resultMessage);
                game.resetGame();
                return true;
            }

            if (game.isBoardFull()) {
                output.writeObject("DRAW!");
                game.resetGame();
                return true;
            }

            return false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while processing end game: {0}", e.getMessage());
            return false;
        }
    }

    private int minimax(TicTacToeGame game, int depth, boolean isMaximizing, String aiSymbol) {
        String playerSymbol = aiSymbol.equals("X") ? "O" : "X";

        if (game.checkWinForSymbol(aiSymbol)) return 10 - depth;
        if (game.checkWinForSymbol(playerSymbol)) return depth - 10;
        if (game.isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (game.getBoard()[i][j].equals(" ")) {
                        game.getBoard()[i][j] = aiSymbol;
                        int score = minimax(game, depth + 1, false, aiSymbol);
                        game.getBoard()[i][j] = " ";
                        bestScore = Math.max(bestScore, score);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (game.getBoard()[i][j].equals(" ")) {
                        game.getBoard()[i][j] = playerSymbol;
                        int score = minimax(game, depth + 1, true, aiSymbol);
                        game.getBoard()[i][j] = " ";
                        bestScore = Math.min(bestScore, score);
                    }
                }
            }
            return bestScore;
        }
    }
}