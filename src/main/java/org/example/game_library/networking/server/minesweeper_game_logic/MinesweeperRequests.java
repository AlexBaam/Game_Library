package org.example.game_library.networking.server.minesweeper_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.networking.server.tictactoe_game_logic.ScoreEntry;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperRequests {
    private static final Logger logger = AppLogger.getLogger();

    /**
     * Handles requests for Minesweeper scoreboard data.
     * Uses the total_score from the database implicitly.
     */
    // Semnătura metodei s-a schimbat: am eliminat 'String scoreType'
    public static void handleScore(List<String> request, ThreadCreator threadCreator, ObjectOutputStream output, ObjectInputStream input, UserRepository userRepository) {
        try {
            // Nu mai e nevoie de validarea scoreType aici, deoarece nu mai vine din client
            // și UserRepository.getMinesweeperTopRankedPlayers nu mai primește acest parametru.

            List<ScoreEntry> topPlayers = userRepository.getMinesweeperTopRankedPlayers(3); // Obținem top 3 jucători

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