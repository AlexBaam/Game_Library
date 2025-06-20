package org.example.game_library.views.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.ui.ShowAlert;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button newGameButton;

    @FXML
    public Button loadGameButton;

    @FXML
    public Button scoreboardButton;

    @FXML
    private GridPane boardGrid;

    @FXML
    private TextField usernameField;

    @FXML
    private Button scoreButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button backButton;

    private String currentSymbol;

    @FXML
    public void onExitClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed exit button.");
        try {
            ClientToServerProxy.send(List.of("exit"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not send exit command to server: {0}", e.getMessage());
        } finally {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void onScoreClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed score button. Navigating to Scoreboard.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/scoreForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Game Library - Tic Tac Toe Scoreboard");
            stage.show();
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Error loading scores", "Could not load the scores screen.: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load scoreForm.fxml: {0}", e.getMessage());
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (TicTacToe)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/userDashboardForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to user dashboard.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load the Dashboard: {0}", e.getMessage());
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Eroare de navigare", "Nu s-a putut intoarce la meniul principal.");
        }
    }

    @FXML
    public void onNewGameClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed new game button. (TicTacToe)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeNewGameScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TicTacToe - New Game");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Eroare", "Could not load TicTacToe game.");
        }
    }

    @FXML
    public void onLoadClick(ActionEvent event) {
        try {
            ClientToServerProxy.send(List.of("tictactoe", "load"));

            Object obj = ClientToServerProxy.receive();

            if (obj instanceof String response) {
                if (response.startsWith("FAILURE")) {
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "Eroare la incarcare", response);
                    return;
                }
            }

            if (obj instanceof TicTacToeGame loadedGame) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeBoard.fxml"));
                Parent root = loader.load();

                TicTacToeBoard controller = loader.getController();
                controller.loadGameToUI(loadedGame);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TicTacToe - Loaded Game");
                stage.show();
            } else {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Unexpected", "Unknown response from server.");
            }
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Error", "Could not load game: " + e.getMessage());
        }
    }
}

