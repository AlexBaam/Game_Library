package org.example.game_library.networking.server.minesweeper_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntryM;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperRequests {
    private static final Logger logger = AppLogger.getLogger();

    public static void handleNewGame(List<String> request, ThreadCreator thread, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        // Exemplu: parsezi modul de joc
        if (request.size() < 3) {
            output.writeObject("Eroare: Mod de joc lipsa");
            return;
        }

        String mode = request.get(2); // "easy", "medium", "hard"

        // Aici creezi jocul efectiv - pentru acum poate fi doar un mesaj
        // Mai tarziu poti genera tabla si salva intr-un hashmap cu userul
        System.out.println("Creating new Minesweeper game with mode: " + mode);

        output.writeObject("SUCCESS: Game created with mode " + mode);
    }

    public static void handleReveal(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            // Extragem coordonatele
            int x = Integer.parseInt(request.get(1));
            int y = Integer.parseInt(request.get(2));

            // Obținem starea jocului pentru thread-ul curent
            MinesweeperGameState gameState = threadCreator.getMinesweeperGameState();

            if (gameState == null) {
                output.writeObject("ERROR: No game in progress.");
                return;
            }

            String result;
            if (gameState.revealCell(x, y)) {
                result = "Cell revealed, game over!";
            } else {
                result = "Cell revealed successfully!";
            }


            // Trimitem starea jocului și rezultatul acțiunii
            output.writeObject(gameState.getBoardForClient());
            output.writeObject(result);

            logger.log(Level.INFO, "Cell revealed at ({0},{1}) for thread {2}", new Object[]{x, y, threadCreator.getId()});

        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARNING, "Invalid coordinates for reveal for thread {0}", threadCreator.getId());
            try {
                output.writeObject("ERROR: Invalid coordinates.");
            } catch (IOException ignored) {}
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in handleReveal for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            try {
                output.writeObject("ERROR: An unexpected error occurred during reveal.");
            } catch (IOException ignored) {}
        }
    }

    public static void handleFlag(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input) {
        try {
            // Extragem coordonatele
            int x = Integer.parseInt(request.get(1));
            int y = Integer.parseInt(request.get(2));

            // Obținem starea jocului pentru thread-ul curent
            MinesweeperGameState gameState = threadCreator.getMinesweeperGameState();

            if (gameState == null) {
                output.writeObject("ERROR: No game in progress.");
                return;
            }

            // Apelăm metoda de punere a steguletului
            String result = gameState.flagCell(x, y);

            // Trimitem starea jocului și rezultatul acțiunii
            output.writeObject(gameState.getBoardForClient());
            output.writeObject(result);

            logger.log(Level.INFO, "Cell flagged at ({0},{1}) for thread {2}", new Object[]{x, y, threadCreator.getId()});

        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARNING, "Invalid coordinates for flag for thread {0}", threadCreator.getId());
            try {
                output.writeObject("ERROR: Invalid coordinates.");
            } catch (IOException ignored) {}
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in handleFlag for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            try {
                output.writeObject("ERROR: An unexpected error occurred during flagging.");
            } catch (IOException ignored) {}
        }
    }

    //public static void handleLoadGame(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input)
    //public static void handleForfeit(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input)
    //public static void handleSaveGame(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input)

    public static void handleScore(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input, UserRepository userRepository) {
        try {
            List<ScoreEntryM> topPlayers = userRepository.getMinesweeperTopRankedPlayers(3);

            output.writeObject(topPlayers);
            logger.log(Level.INFO, "Sent Minesweeper top ranked players to client for thread {0}.", threadCreator.getId());

        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Database error retrieving Minesweeper scores for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            try {
                output.writeObject("ERROR: Database error retrieving Minesweeper scores.");
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Error sending database error to client for thread {0}: {1}", new Object[]{threadCreator.getId(), ioException.getMessage()});
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending Minesweeper scores to client for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in MinesweeperRequests.handleScore for thread {0}: {1}", new Object[]{threadCreator.getId(), e.getMessage()});
            try {
                output.writeObject("ERROR: An unexpected error occurred while retrieving Minesweeper scores.");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error sending unexpected error to client for thread {0}: {1}", new Object[]{threadCreator.getId(), ex.getMessage()});
            }
        }
    }
}