package org.example.game_library.networking.server.tictactoe_game_logic;

import org.example.game_library.networking.server.ThreadCreator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class AiHandler implements MoveHandler {
    @Override
    public void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        int row = Integer.parseInt(request.get(2));
        int col = Integer.parseInt(request.get(3));
        String symbol = "X";

        TicTacToeGame game = threadCreator.getTicTacToeGame();

        if (!symbol.equals(game.getCurrentSymbol())) {
            output.writeObject("FAILURE: Not your turn!");
            return;
        }

        if (!game.makeMove(row, col, symbol)) {
            output.writeObject("FAILURE: Move failed! Cell already occupied!");
            return;
        }

        if (game.checkWin()) {
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
    }
}
