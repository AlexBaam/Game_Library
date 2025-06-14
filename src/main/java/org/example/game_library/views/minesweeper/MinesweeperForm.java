package org.example.game_library.views.minesweeper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.ui.ShowAlert;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button newGameButton;

    @FXML
    public Button loadGameButton;

    @FXML
    public Button scoreboardButton;

    @FXML
    private Button scoreButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button backButton;

    @FXML
    private void onNewGameClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed new game button. (Minesweeper)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Minesweeper - New Game");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Eroare", "Could not load Minesweeper game.");
        }
    }

    @FXML
    private void onLoadGameClick(ActionEvent event) {
        ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Load Game", "The Minesweeper 'Load Game' functionality is not yet implemented.");
        logger.log(Level.INFO, "Load Game Minesweeper clicked.");

    }

    @FXML
    private void onScoreboardClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/scoreForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigat la clasamentul Minesweeper.");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading the Minesweeper Scoreboard: {0}",  e.getMessage());
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Eroare de navigare", "Unable to display Minesweeper leaderboard.");
        }
    }

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (Minesweeper)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/userDashboardForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to user dashboard.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load the user dashboard: {0}", e.getMessage());
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Navigation error", "Couldn't load the user dashboard.");
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        logger.log(Level.INFO, "Exit Minesweeper clicked.");
        try {
            ClientToServerProxy.send(List.of("exit"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not send exit command to server: {0}", e.getMessage());
        } finally {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void onSaveClick(ActionEvent actionEvent) {
    }

    public void onForfeitClick(ActionEvent actionEvent) {
    }
}