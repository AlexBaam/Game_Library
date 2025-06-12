package org.example.game_library.networking.server.minesweeper_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.repository.MinesweeperRepository;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperRequests {

    private MinesweeperRequests(){
    }

    private static final Logger logger = AppLogger.getLogger();

    public static void handleNewGame(List<String> request, ThreadCreator thread, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        MinesweeperGameState game = null;
        String statusMessage = "ERROR: Uninitialized status message.";

        try {
            if (request.size() < 3) {
                statusMessage = "ERROR: Missing game mode";
            } else {
                String mode = request.get(2);
                int rows = 9, cols = 9, mines = 10;

                switch (mode.toLowerCase()) {
                    case "medium":
                        rows = cols = 16;
                        mines = 40;
                        break;
                    case "hard":
                        rows = cols = 24;
                        mines = 99;
                        break;
                    case "easy":
                    default:
                        break;
                }

                game = new MinesweeperGameState(rows, cols);
                game.placeMines(mines);
                game.calculateNumbers();

                thread.setMinesweeperGameState(game);
                statusMessage = "SUCCESS: Game created with mode " + mode;
                logger.log(Level.INFO, "Creating new Minesweeper game with mode: {0}", mode);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating Minesweeper game: {0}", e.getMessage());
            statusMessage = "ERROR: Internal server error during game creation - " + e.getMessage();
            game = null;
        } finally {
            output.writeObject(game);
            output.writeObject(statusMessage);
            output.flush();
        }
    }

    public static void handleForfeit(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        String statusMessage = "ERROR: Failed to process forfeit.";
        try {
            threadCreator.setMinesweeperGameState(null);
            statusMessage = "SUCCESS";
            logger.log(Level.INFO, "Game forfeited for thread {0}", threadCreator.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling forfeit for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            statusMessage = "ERROR: " + e.getMessage();
        } finally {
            try {
                output.writeObject(statusMessage);
                output.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending forfeit response for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            }
        }
    }

    public static void handleScore(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input, UserRepository userRepository) {
        Object responseObj = null;
        try {
            List<ScoreEntryM> topPlayers = MinesweeperRepository.getMinesweeperTopRankedPlayers(3);
            output.writeObject(topPlayers);
            logger.log(Level.INFO, "Sent Minesweeper top ranked players to client for thread {0}.", threadCreator.getId());
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Database error retrieving Minesweeper scores for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            responseObj = "ERROR: Database error retrieving Minesweeper scores.";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in MinesweeperRequests.handleScore for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            responseObj = "ERROR: An unexpected error occurred while retrieving Minesweeper scores.";
        } finally {
            try {
                output.writeObject(responseObj);
                output.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending Minesweeper scores response for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            }
        }
    }


    public static void handleFlag(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        MinesweeperGameState gameState = null;
        String result = "ERROR: Unknown error during flag.";

        try {
            if (request.size() < 4) {
                result = "ERROR: Insufficient arguments for flag command.";
                logger.log(Level.WARNING, "Insufficient arguments for flag command for thread {0}. Request: {1}",
                        new Object[]{threadCreator.getId(), request});
                return;
            }

            int x = Integer.parseInt(request.get(2));
            int y = Integer.parseInt(request.get(3));

            gameState = threadCreator.getMinesweeperGameState();

            if (gameState == null) {
                result = "ERROR: No game in progress.";
            } else {
                gameState.toggleFlag(x, y);
                result = "Cell flag toggled successfully!";
                logger.log(Level.INFO, "Cell flag toggled at ({0},{1}) for thread {2}. Result: {3}", new Object[]{x, y, threadCreator.getId(), result});
            }

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Non-numeric coordinates for flag for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            result = "ERROR: Invalid coordinate format.";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in handleFlag for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            result = "ERROR: An unexpected error occurred during flagging.";
        } finally {
            try {
                output.writeObject(gameState);
                output.writeObject(result);
                output.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending response in handleFlag for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            }
        }
    }

    public static void handleShovel(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        MinesweeperGameState gameState = null;
        String result = "ERROR: Unknown error during shovel.";

        try {
            if (request.size() < 4) {
                result = "ERROR: Insufficient arguments for shovel command.";
                logger.log(Level.WARNING, "Insufficient arguments for shovel command for thread {0}. Request: {1}",
                        new Object[]{threadCreator.getId(), request});
                return;
            }

            int x = Integer.parseInt(request.get(2));
            int y = Integer.parseInt(request.get(3));

            gameState = threadCreator.getMinesweeperGameState();

            if (gameState == null) {
                result = "ERROR: No game in progress.";
            } else {
                boolean gameOver = gameState.revealCell(x, y);
                result = gameOver ? "Cell revealed, game over!" : "Cell revealed successfully!";
                logger.log(Level.INFO, "Cell revealed at ({0},{1}) for thread {2}. Game over: {3}", new Object[]{x, y, threadCreator.getId(), gameOver});
            }

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Non-numeric coordinates for shovel for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            result = "ERROR: Invalid coordinate format.";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in handleShovel for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            result = "ERROR: An unexpected error occurred during shovel.";
        } finally {
            try {
                output.writeObject(gameState);
                output.writeObject(result);
                output.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending response in handleShovel for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            }
        }
    }
}