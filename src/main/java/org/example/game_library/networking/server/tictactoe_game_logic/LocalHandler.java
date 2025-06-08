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

public class LocalHandler implements MoveHandler {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        if (request.size() < 5) {
            output.writeObject("FAILURE: Missing symbol for local mode!");
            return;
        }

        int row = Integer.parseInt(request.get(2));
        int col = Integer.parseInt(request.get(3));
        String symbol = request.get(4).toUpperCase();

        TicTacToeGame game = threadCreator.getTicTacToeGame();
        if (!symbol.equals("X") && !symbol.equals("O")) {
            output.writeObject("FAILURE: Invalid symbol! Must be X or O!");
            return;
        }

        if (!symbol.equals(game.getCurrentSymbol())) {
            output.writeObject("FAILURE: Not your turn!");
            return;
        }

        processMove(game, row, col, symbol, output, threadCreator);
    }

    private void processMove(TicTacToeGame game, int row, int col, String symbol, ObjectOutputStream output, ThreadCreator threadCreator){
        try{
            if (!game.makeMove(row, col, symbol)) {
                output.writeObject("FAILURE: Move failed! Cell already occupied!");
                return;
            }

            if (game.checkWin()) {
                User currentUser = threadCreator.getCurrentUser();
                if (currentUser != null) {
                    try {
                        TicTacToeRepository.incrementWins(currentUser, "local");
                    } catch (Exception e) {
                        System.err.println("Could not update score: " + e.getMessage());
                    }
                }
                output.writeObject("WIN: " + symbol);
                game.resetGame();
                return;
            }

            if (game.isBoardFull()) {
                output.writeObject("DRAW!");
                game.resetGame();
                return;
            }

            game.togglePlayer();
            output.writeObject("SUCCESS");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while processing move: {0}", e.getMessage());
        }
    }
}
