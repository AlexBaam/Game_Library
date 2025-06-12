package org.example.game_library.views.minesweeper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.minesweeper_game_logic.MinesweeperGameState;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperNewGameScreen {

    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public void onEasyClick(ActionEvent event) {
        startGame("easy", event);
    }

    @FXML
    public void onMediumClick(ActionEvent event) {
        startGame("medium", event);
    }

    @FXML
    public void onHardClick(ActionEvent event) {
        startGame("hard", event);
    }

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (Minesweeper - New Game)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to main menu.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back to main menu.");
        }
    }

    private void startGame(String modeRequested, ActionEvent event) {
        try {
            ClientToServerProxy.send(List.of("minesweeper", "newgame", modeRequested));

            // Citim PRIMUL obiect: Așteptăm MinesweeperGameState (poate fi null în caz de eroare)
            Object firstReceivedObject = ClientToServerProxy.receive();
            // Citim AL DOILEA obiect: Așteptăm String-ul de răspuns (SUCCESS/ERROR)
            Object secondReceivedObject = ClientToServerProxy.receive();

            MinesweeperGameState initialGameState = null;
            String statusResponse = "ERROR: Unknown server response.";

            // Verificăm și castăm obiectele primite
            if (firstReceivedObject instanceof MinesweeperGameState) {
                initialGameState = (MinesweeperGameState) firstReceivedObject;
            }
            if (secondReceivedObject instanceof String) {
                statusResponse = (String) secondReceivedObject;
            }

            // Acum verificăm dacă jocul a fost inițializat cu succes
            if (initialGameState != null && statusResponse.toLowerCase().startsWith("success")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperBoard.fxml"));
                Parent root = loader.load();

                MinesweeperBoard controller = loader.getController();
                controller.setInitialGameState(initialGameState); // Transmite starea jocului către controller

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Minesweeper - " + modeRequested.toUpperCase() + " Game");
                stage.show();
                logger.log(Level.INFO, "Game started successfully for mode: {0}", modeRequested);

            } else {
                // Dacă nu a fost un succes, afișăm eroarea primită de la server
                String errorMessage = "Failed to start new game.";
                errorMessage += " Server response: " + statusResponse; // Folosim statusResponse care a fost parsată
                logger.log(Level.WARNING, errorMessage);
                showAlert(Alert.AlertType.ERROR, "Game Initialization Error", errorMessage);
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error during game initialization: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Network Error", "An error occurred while starting the game: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}