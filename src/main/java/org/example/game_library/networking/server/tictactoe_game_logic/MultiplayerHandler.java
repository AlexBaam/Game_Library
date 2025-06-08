package org.example.game_library.networking.server.tictactoe_game_logic;

import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiplayerHandler implements MoveHandler {
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        int row = Integer.parseInt(request.get(2));
        int col = Integer.parseInt(request.get(3));

        Room room = RoomManager.getRoomByPlayer(threadCreator);
        if (room == null || !room.containsPlayer(threadCreator)) {
            output.writeObject("FAILURE: Not in a multiplayer room.");
            return;
        }

        if (!room.isPlayerTurn(threadCreator)) {
            output.writeObject("FAILURE: Not your turn!");
            return;
        }

        TicTacToeGame game = room.getGame();
        String symbol = room.getPlayerSymbol(threadCreator);
        ObjectOutputStream opponentOut = room.getOtherPlayerOutput(threadCreator);

        if (!game.makeMove(row, col, symbol)) {
            output.writeObject("FAILURE: Cell already occupied!");
            return;
        }

        if (game.checkWin()) {
            output.writeObject("WIN: " + symbol);
            try {
                if (opponentOut != null)
                    opponentOut.writeObject("LOSE: " + symbol);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to notify opponent: {0}", e.getMessage());
            }
            game.resetGame();
            RoomManager.removeRoom(room.getRoomId());
            return;
        }

        if (game.isBoardFull()) {
            output.writeObject("DRAW!");
            try {
                if (opponentOut != null)
                    opponentOut.writeObject("DRAW!");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to notify opponent of DRAW: {0}", e.getMessage());
            }
            game.resetGame();
            RoomManager.removeRoom(room.getRoomId());
            return;
        }

        game.togglePlayer();
        output.writeObject("SUCCESS");
        if (opponentOut != null)
            opponentOut.writeObject("OPPONENT_MOVED:" + row + "," + col);
    }
}
