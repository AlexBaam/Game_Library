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

        boolean aiMoved = false;
        int aiRow = -1, aiCol = -1;
        outerLoop:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (game.getBoard()[i][j].equals(" ")) {
                    game.makeMove(i, j, aiSymbol);
                    aiRow = i;
                    aiCol = j;
                    aiMoved = true;
                    break outerLoop;
                }
            }
        }

        if (!aiMoved) {
            output.writeObject("DRAW!");
            game.resetGame();
            return;
        }

        output.writeObject("AI_MOVE:" + aiRow + "," + aiCol);

        if (game.checkWin()) {
            output.writeObject("LOSE: " + aiSymbol);
            game.resetGame();
            return;
        }

        if (game.isBoardFull()) {
            output.writeObject("DRAW!");
            game.resetGame();
            return;
        }

        game.setCurrentSymbol("X");
        output.writeObject("SUCCESS");
    }

}
