package org.example.game_library.networking.server.tictactoe_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.SavedGameRepository;
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
                    output.writeObject("SUCCESS:mode=local");
                    logger.log(Level.INFO, "New LOCAL game started for thread {0}", threadCreator.getId());
                }
                case "ai" -> {
                    TicTacToeGame game = new TicTacToeGame();
                    game.setMode("ai");
                    threadCreator.setTicTacToeGame(game);
                    output.writeObject("SUCCESS:mode=ai");
                    logger.log(Level.INFO, "New AI game started for thread {0}", threadCreator.getId());
                }
                case "host" -> {
                    String roomId = RoomManager.createRoom(threadCreator);
                    TicTacToeGame game = RoomManager.getRoom(roomId).getGame();
                    game.setMode("network");
                    threadCreator.setTicTacToeGame(game);
                    output.writeObject("SUCCESS:mode=network;room=" + roomId);
                }
                case "join" -> {
                    if (request.size() < 4) {
                        output.writeObject("FAILURE: Room ID not specified.");
                        return;
                    }

                    String roomId = request.get(3).toUpperCase();
                    Room room = RoomManager.getRoom(roomId);
                    if (room == null || room.isFull()) {
                        output.writeObject("FAILURE: Room unavailable.");
                        return;
                    }

                    room.setGuest(threadCreator);
                    TicTacToeGame game = room.getGame();
                    game.setMode("network");
                    threadCreator.setTicTacToeGame(game);

                    output.writeObject("SUCCESS:mode=network;symbol=" + room.getPlayerSymbol(threadCreator));
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
            if (request.size() < 4) {
                output.writeObject("FAILURE: Invalid move request format!");
                return;
            }

            String mode = threadCreator.getTicTacToeGame().getMode();
            MoveHandler handler = switch (mode) {
                case "local" -> new LocalHandler();
                case "ai" -> new AiHandler();
                case "network" -> new MultiplayerHandler();
                default -> null;
            };

            if (handler == null) {
                output.writeObject("FAILURE: Unsupported game mode.");
                return;
            }

            handler.handleMove(request, threadCreator, output, input);

        } catch (Exception e) {
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