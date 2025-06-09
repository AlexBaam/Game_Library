package org.example.game_library.networking.server.minesweeper_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.enums.CommandMinesweeper;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntryM;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.accessibility.AccessibleAction.CLICK;

public class MinesweeperRequests {
    private static final Logger logger = AppLogger.getLogger();

    public static void handleMinesweeperRequest(List<String> request, ThreadCreator clientThread,
                                                ObjectOutputStream output, ObjectInputStream input,
                                                User currentUser, MinesweeperGame currentGame) throws IOException {
        if (request.size() == 1) {
            // Clientul vrea doar să intre în meniul Minesweeper
            output.writeObject("SUCCESS_MINESWEEPER_MENU");
            logger.log(Level.INFO, "User {0} successfully entered Minesweeper menu.", currentUser.getUsername());
            return; // Am terminat de procesat request-ul
        }

        // Dacă ajungem aici, înseamnă că request.size() >= 2, deci ar trebui să avem o subcomandă
        String subCommandStr = request.get(1).toLowerCase();
        CommandMinesweeper subCommand = CommandMinesweeper.fromString(subCommandStr);

        if (subCommand == null) {
            output.writeObject("FAILURE: Invalid Minesweeper subcommand: " + subCommandStr);
            return;
        }

        switch (subCommand) {
            case NEWGAME:
                handleNewGame(request, clientThread, output, currentUser);
                break;
            case CLICK:
                if (currentGame == null) {
                    output.writeObject("FAILURE: No active Minesweeper game found for this session.");
                    return;
                }
                handleClick(request, output, currentGame);
                break;
            case FORFEIT:
                if (currentGame == null) {
                    output.writeObject("FAILURE: No active Minesweeper game to forfeit.");
                    return;
                }
                handleForfeit(output, currentGame);
                break;
            case SAVE:
                if (currentGame == null) {
                    output.writeObject("FAILURE: No active Minesweeper game to save.");
                    return;
                }
                handleSaveGame(request, output, currentGame, currentUser);
                break;
            case LOAD:
                handleLoadGame(request, output, currentUser);
                break;
            case SCORE:
                // Aici ar trebui să apelezi handleScore cu UserRepository, dacă este necesar
                // presupunând că ai o metodă handleScore definită cu userRepository
                // handleScore(request, clientThread, output, input, new UserRepository()); // Exemplu
                handleScoreRequest(request, output); // Păstrăm apelul tău existent, dar vezi mai jos
                break;
            default:
                output.writeObject("FAILURE: Minesweeper subcommand not implemented: " + subCommandStr);
        }
    }

    private static void handleScoreRequest(List<String> request, ObjectOutputStream output) {

    }

    private static void handleNewGame(List<String> request, ThreadCreator clientThread, ObjectOutputStream output, User currentUser) throws IOException {
        if (request.size() < 3) {
            output.writeObject("FAILURE: Not enough arguments for NEWGAME command (difficulty missing).");
            return;
        }
        String difficultyStr = request.get(2).toLowerCase();
        int rows, cols, totalMines;

        switch (difficultyStr) {
            case "easy":
                rows = 9; cols = 9; totalMines = 10;
                break;
            case "medium":
                rows = 16; cols = 16; totalMines = 40;
                break;
            case "hard":
                rows = 16; cols = 30; totalMines = 99;
                break;
            default:
                output.writeObject("FAILURE: Invalid difficulty level: " + difficultyStr);
                return;
        }

        MinesweeperGame newGame = new MinesweeperGame(rows, cols, totalMines);
        // Trebuie să stocăm această instanță de joc undeva, asociată cu thread-ul/clientul.
        // Asta înseamnă că ThreadCreator va avea nevoie de o proprietate MinesweeperGame
        // similar cu TicTacToeGame ticTacToeGame;
        clientThread.setCurrentMinesweeperGame(newGame); // Presupunem că ThreadCreator are această metodă

        // Trimitem înapoi clientului parametrii tablei
        output.writeObject("SUCCESS;rows=" + rows + ";cols=" + cols + ";mines=" + totalMines);
        logger.log(Level.INFO, "New Minesweeper game started for user {0}, difficulty {1}.", new Object[]{currentUser.getUsername(), difficultyStr});
    }

    private static void handleClick(List<String> request, ObjectOutputStream output, MinesweeperGame game) throws IOException {
        if (request.size() < 5) {
            output.writeObject("FAILURE: Not enough arguments for CLICK command (row, col, type missing).");
            return;
        }

        try {
            int row = Integer.parseInt(request.get(2));
            int col = Integer.parseInt(request.get(3));
            String clickType = request.get(4);

            List<Cell> updatedCells = new ArrayList<>();
            if ("primary".equals(clickType)) {
                updatedCells = game.revealCell(row, col);
            } else if ("secondary".equals(clickType)) {
                updatedCells = game.toggleFlag(row, col);
            } else {
                output.writeObject("FAILURE: Unknown click type: " + clickType);
                return;
            }

            // Trimitem lista de celule actualizate și starea jocului
            List<Object> response = new ArrayList<>();
            if (game.isGameOver()) {
                response.add("GAME_OVER");
            } else if (game.isGameWon()) {
                response.add("GAME_WON");
            } else {
                response.add("BOARD_UPDATE"); // Spunem clientului că e o actualizare a tablei
            }
            response.addAll(updatedCells); // Adaugăm celulele actualizate

            output.writeObject(response);

            // Trimite și numărul de mine rămase separat
            List<Object> minesLeftResponse = new ArrayList<>();
            minesLeftResponse.add("MINES_LEFT");
            minesLeftResponse.add(game.getTotalMines() - game.getFlaggedCellsCount()); // Assuming these getters exist in MinesweeperGame
            output.writeObject(minesLeftResponse);

            logger.log(Level.INFO, "Minesweeper game state updated after click ({0},{1},{2}). Game Over: {3}, Game Won: {4}",
                    new Object[]{row, col, clickType, game.isGameOver(), game.isGameWon()});

        } catch (NumberFormatException e) {
            output.writeObject("FAILURE: Invalid row or column format.");
            logger.log(Level.WARNING, "Invalid row/col format in click request: {0}", request);
        } catch (Exception e) {
            output.writeObject("FAILURE: An error occurred during click processing: " + e.getMessage());
            logger.log(Level.SEVERE, "Error processing Minesweeper click: {0}");
        }
    }

    private static void handleForfeit(ObjectOutputStream output, MinesweeperGame game) throws IOException {
        game.setGameOver(true); // Marchează jocul ca pierdut
        List<Object> response = new ArrayList<>();
        response.add("GAME_OVER");
        // Trimite și toate minele descoperite la forfeit
        List<Cell> revealedCells = new ArrayList<>();
        // Trebuie să apelezi o metodă în game pentru a "dezvălui" toate minele pentru client
        // Aici ai putea crea o metodă revealAllMines() în MinesweeperGame care returnează toate celulele minate
        // Deocamdată, vom lăsa doar mesajul de Game Over.
        output.writeObject(response);
        logger.log(Level.INFO, "Minesweeper game forfeited.");
    }

    private static void handleSaveGame(List<String> request, ObjectOutputStream output, MinesweeperGame game, User currentUser) throws IOException {
        output.writeObject("FAILURE: Save Game not yet implemented.");
        logger.log(Level.INFO, "Save Minesweeper game requested, but not implemented.");
        // Aici va veni logica de salvare (serializarea MinesweeperGame și stocarea în DB)
    }

    private static void handleLoadGame(List<String> request, ObjectOutputStream output, User currentUser) throws IOException {
        output.writeObject("FAILURE: Load Game not yet implemented.");
        logger.log(Level.INFO, "Load Minesweeper game requested, but not implemented.");
        // Aici va veni logica de încărcare
    }

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