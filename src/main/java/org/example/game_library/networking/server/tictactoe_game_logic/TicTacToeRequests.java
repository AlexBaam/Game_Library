package org.example.game_library.networking.server.tictactoe_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.SavedGameRepository;
import org.example.game_library.database.repository.TicTacToeRepository;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeRequests {
    private static final Logger logger = AppLogger.getLogger();

    public static void handleNewGame(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            if (request.size() < 3) {
                output.writeObject("FAILURE: Game mode not specified.");
                logger.log(Level.WARNING, "Game mode missing in NEWGAME request (thread {0})", threadCreator.getId());
                return;
            }

            String mode = request.get(2).toLowerCase();

            switch (mode) {
                case "local" -> {
                    TicTacToeGame game = new TicTacToeGame();
                    game.setMode("local");
                    threadCreator.setTicTacToeGame(game);
                    output.writeObject("SUCCESS");
                    logger.log(Level.INFO, "New LOCAL game started for thread {0}", threadCreator.getId());
                }
                case "player" -> {
                    TicTacToeGame game = new TicTacToeGame();
                    game.setMode("network");
                    threadCreator.setTicTacToeGame(game);
                    output.writeObject("SUCCESS");
                    logger.log(Level.INFO, "New NETWORK game started for thread {0}", threadCreator.getId());
                }
                case "ai" -> {
                    TicTacToeGame game = new TicTacToeGame();
                    game.setMode("ai");
                    threadCreator.setTicTacToeGame(game);
                    output.writeObject("SUCCESS");
                    logger.log(Level.INFO, "New AI game started for thread {0}", threadCreator.getId());
                }
                default -> {
                    output.writeObject("FAILURE: Game mode not supported.");
                    logger.log(Level.WARNING, "Unsupported game mode '{0}' from thread {1}", new Object[]{mode, threadCreator.getId()});
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to output stream: {0}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void handleLoadGame(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            User currentUser = threadCreator.getCurrentUser();
            List<TicTacToeGame> games = SavedGameRepository.loadGamesForUser(currentUser, "tictactoe");

            if (games.isEmpty()) {
                output.writeObject("FAILURE: No saved games found.");
            } else {
                TicTacToeGame lastGame = games.get(0);
                threadCreator.setTicTacToeGame(lastGame);
                output.writeObject(lastGame);
            }

        } catch (Exception e) {
            try {
                output.writeObject("FAILURE: Could not load game - " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void handleScore(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input, String scoreType, UserRepository userRepository) {
        try {
            List<ScoreEntry> topPlayers = userRepository.getTicTacToeTopRankedPlayers(3, scoreType);
            output.writeObject(topPlayers);
            logger.log(Level.INFO, "Sent TicTacToe top ranked players (type: {0}) to client for thread {1}.", new Object[]{scoreType, threadCreator.getId()});
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid score type requested: {0} - {1}", new Object[]{scoreType, e.getMessage()});
            try {
                output.writeObject("ERROR: " + e.getMessage());
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Error sending error to client: " + ioException.getMessage());
            }
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Database error retrieving TicTacToe scores: " + e.getMessage());
            try {
                output.writeObject("ERROR: Database error retrieving scores.");
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Error sending database error to client: " + ioException.getMessage());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending scores to client: " + e.getMessage());
        }
    }

    public static void handleForfeit(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try{
            if(request.size() == 2) {
                output.writeObject("SUCCESS");
            } else {
                output.writeObject("Request didn't have enough arguments!");
                logger.log(Level.WARNING, "Forfeit failed! Reason send back to the client!");
            }
        } catch (IOException e){
            logger.log(Level.SEVERE, "Error writing to output stream: {0}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void handleMove(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            if(request.size() < 5) {
                output.writeObject("FAILURE: Invalid move request format!");
                return;
            }

            int row = Integer.parseInt(request.get(2));;
            int col = Integer.parseInt(request.get(3));;
            String symbol = request.get(4).toUpperCase();

            TicTacToeGame game = threadCreator.getTicTacToeGame();

            if(game == null) {
                output.writeObject("FAILURE: No active game session!");
                return;
            }

            if(!symbol.equals("X") && !symbol.equals("O")) {
                output.writeObject("FAILURE: Invalid symbol! Must be X or O!");
                return;
            }

            if(!symbol.equals(String.valueOf(game.getCurrentSymbol()))) {
                output.writeObject("FAILURE: Wait for your turn!");
                return;
            }

            boolean moveResult = game.makeMove(row, col, symbol);

            if(!moveResult) {
                output.writeObject("FAILURE: Move failed! Cell already occupied!");
                return;
            }

            if(game.checkWin()){
                try{
                    User currentUser = threadCreator.getCurrentUser();
                    TicTacToeRepository.incrementWins(currentUser, game.getMode());
                    output.writeObject("WIN: " + symbol);
                    game.resetGame();
                    return;
                } catch (PersistenceException e) {
                    logger.log(Level.SEVERE, "SQL exception met: {0}", e.getMessage());
                }
            }

            if(game.isBoardFull()){
                output.writeObject("DRAW!");
                game.resetGame();
                return;
            }

            game.togglePlayer();
            output.writeObject("SUCCESS");
        } catch (IOException | NumberFormatException e) {
            try {
                output.writeObject("FAILURE: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void handleSaveGame(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            User currentUser = threadCreator.getCurrentUser();
            TicTacToeGame game = threadCreator.getTicTacToeGame();

            if (game == null) {
                output.writeObject("FAILURE: No game in progress to save.");
                return;
            }

            SavedGameRepository.saveGame(currentUser, "tictactoe", game);
            output.writeObject("SUCCESS");

        } catch (Exception e) {
            try {
                output.writeObject("FAILURE: Could not save game - " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}