package org.example.game_library.networking.server.minesweeper_game_logic;

import jakarta.persistence.PersistenceException;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.enums.CommandMinesweeper;
import org.example.game_library.networking.enums.DifficultyLevel;
import org.example.game_library.networking.server.ThreadCreator;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperRequests {
    private static final Logger logger = AppLogger.getLogger();

    public static void handleMinesweeperRequest(List<String> request, ThreadCreator clientThread,
                                                ObjectOutputStream output, ObjectInputStream input, // input e pasat, dar nu e folosit aici, e ok
                                                User currentUser, MinesweeperGame currentGame) throws IOException {
        if (request.size() == 1) {
            output.writeObject("SUCCESS_MINESWEEPER_MENU");
            logger.log(Level.INFO, "User {0} successfully entered Minesweeper menu.", currentUser.getUsername());
            return;
        }

        String subCommandStr = request.get(1).toLowerCase();
        CommandMinesweeper subCommand = CommandMinesweeper.fromString(subCommandStr);

        if (subCommand == null ) {
            output.writeObject("FAILURE: Invalid Minesweeper subcommand: " + subCommandStr);
            return;
        }

        // Folosim currentGame pasat, care este currentMinesweeperGame din ThreadCreator
        // și pe care o setăm prin clientThread.setCurrentMinesweeperGame(newGame);
        MinesweeperGame game = clientThread.getCurrentMinesweeperGame(); // Ne asigurăm că folosim instanța corectă

        switch (subCommand) {
            case NEWGAME:
                // Aici se creează un joc nou și se setează în ThreadCreator
                handleNewGame(request, clientThread, output, currentUser);
                break;
            case CLICK:
                if (game == null) { // Verificăm instanța jocului curent
                    output.writeObject("FAILURE: No active Minesweeper game found for this session. Please start a new game.");
                    return;
                }
                handleClick(request, output, game); // Pasez instanța 'game'
                // Dupa click, verificam daca jocul s-a terminat si curatam pe server
                if (game.isGameOver() || game.isGameWon()) {
                    clientThread.setCurrentMinesweeperGame(null); // Curățăm pe ThreadCreator
                    logger.log(Level.INFO, "Minesweeper game ended (click). Game state cleared for user {0}.", currentUser.getUsername());
                }
                break;
            case FORFEIT:
                if (game == null) { // Verificăm instanța jocului curent
                    output.writeObject("FAILURE: No active Minesweeper game to forfeit.");
                    return;
                }
                handleForfeit(output, game); // Pasez instanța 'game'
                clientThread.setCurrentMinesweeperGame(null); // curatam aici
                logger.log(Level.INFO, "Minesweeper game forfeited. Game state cleared for user {0}.", currentUser.getUsername());
                break;
            case SAVE:
                if (game == null) {
                    output.writeObject("FAILURE: No active Minesweeper game to save.");
                    return;
                }
                handleSaveGame(request, output, game, currentUser);
                break;
            case LOAD:
                // Load trebuie sa seteze jocul in ThreadCreator
                handleLoadGame(request, output, currentUser, clientThread);
                break;
            case SCORE:
                handleScore(request, clientThread, output, input, new UserRepository());
                break;
            default:
                output.writeObject("FAILURE: Minesweeper subcommand not implemented: " + subCommandStr);
        }
    }

    private static void handleNewGame(List<String> request, ThreadCreator clientThread, ObjectOutputStream output, User currentUser) throws IOException {
        if (request.size() < 3) {
            output.writeObject("FAILURE: NEWGAME requires a difficulty level.");
            return;
        }
        String difficultyStr = request.get(2).toUpperCase();
        DifficultyLevel difficulty;
        try {
            difficulty = DifficultyLevel.valueOf(difficultyStr);
        } catch (IllegalArgumentException e) {
            output.writeObject("FAILURE: Invalid difficulty level: " + difficultyStr);
            logger.log(Level.WARNING, "Invalid Minesweeper difficulty level: {0}", difficultyStr);
            return;
        }

        MinesweeperGame newGame = new MinesweeperGame(difficulty);
        clientThread.setCurrentMinesweeperGame(newGame); // SETEAZĂ JOCUL PE THREAD-UL CURENT

        output.writeObject("SUCCESS;rows=" + newGame.getRows() + ";cols=" + newGame.getCols() + ";mines=" + newGame.getTotalMines());
        logger.log(Level.INFO, "User {0} started a new Minesweeper game with difficulty {1}. Board: {2}x{3}, {4} mines.",
                new Object[]{currentUser.getUsername(), difficulty.name(), newGame.getRows(), newGame.getCols(), newGame.getTotalMines()});
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

            // *** CORECTIA MAJORĂ PENTRU COMUNICARE: Combinarea celor două răspunsuri ***
            List<Object> combinedResponse = new ArrayList<>();
            if (game.isGameOver()) {
                combinedResponse.add("GAME_OVER");
            } else if (game.isGameWon()) {
                combinedResponse.add("GAME_WON");
            } else {
                combinedResponse.add("BOARD_UPDATE");
            }
            combinedResponse.add(game.getTotalMines() - game.getFlaggedCellsCount()); // Adaugă minele rămase
            combinedResponse.addAll(updatedCells); // Adaugă celulele actualizate

            output.writeObject(combinedResponse); // Trimite UN SINGUR obiect

            logger.log(Level.INFO, "Minesweeper game state updated after click ({0},{1},{2}). Game Over: {3}, Game Won: {4}",
                    new Object[]{row, col, clickType, game.isGameOver(), game.isGameWon()});

        } catch (NumberFormatException e) {
            output.writeObject("FAILURE: Invalid row or column format.");
            logger.log(Level.WARNING, "Invalid row/col format in click request: {0}", request);
        } catch (Exception e) {
            output.writeObject("FAILURE: An error occurred during click processing: " + e.getMessage());
            logger.log(Level.SEVERE, "Error processing Minesweeper click: {0}", e);
        }
    }

    private static void handleForfeit(ObjectOutputStream output, MinesweeperGame game) throws IOException {
        game.setGameOver(true);
        List<Object> response = new ArrayList<>();
        response.add("GAME_OVER");
        // De asemenea, poți include numărul de mine rămase sau orice altceva în response
        response.add(game.getTotalMines() - game.getFlaggedCellsCount()); // Include mines left at forfeit
        // Și celulele (dacă vrei să arăți toate minele la forfeit)
        // for (int r = 0; r < game.getRows(); r++) {
        //    for (int c = 0; c < game.getCols(); c++) {
        //        if (game.getCell(r, c).isMine()) {
        //            game.getCell(r, c).setRevealed(true); // reveal all mines on forfeit
        //            response.add(game.getCell(r, c));
        //        }
        //    }
        // }
        output.writeObject(response);
        logger.log(Level.INFO, "Minesweeper game forfeited.");
    }

    private static void handleSaveGame(List<String> request, ObjectOutputStream output, MinesweeperGame game, User currentUser) throws IOException {
        output.writeObject("FAILURE: Save Game not yet implemented.");
        logger.log(Level.INFO, "Save Minesweeper game requested, but not implemented.");
    }

    private static void handleLoadGame(List<String> request, ObjectOutputStream output, User currentUser, ThreadCreator clientThread) throws IOException {
        output.writeObject("FAILURE: Load Game not yet implemented.");
        logger.log(Level.INFO, "Load Minesweeper game requested, but not implemented.");
        // Exemplu cum ai seta jocul încărcat:
        // MinesweeperGame loadedGame = ... // Încarcă jocul din DB
        // if (loadedGame != null) {
        //     clientThread.setCurrentMinesweeperGame(loadedGame);
        //     output.writeObject("SUCCESS_LOADED;rows=" + loadedGame.getRows() + ";cols=" + loadedGame.getCols() + ";mines=" + loadedGame.getTotalMines());
        // } else {
        //     output.writeObject("FAILURE: No saved game found.");
        // }
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