package org.example.game_library.views.minesweeper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntry;
import org.example.game_library.utils.loggers.AppLogger;

import java.awt.*;
import java.io.IOException;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScoreForm {
    private static final Logger logger = AppLogger.getLogger();


    @FXML
    private TableView<ScoreEntry> scoreTable;

    @FXML
    private TableColumn<ScoreEntry, Integer> rankColumn;

    @FXML
    private TableColumn<ScoreEntry, String> usernameColumn;

    @FXML
    private TableColumn<ScoreEntry, Integer> toatlWinsColumn;

    @FXML
    private TableColumn<ScoreEntry, Integer> bestScoreColumn;

    @FXML
    private Label rankTitleLabel;


    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        toatlWinsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        rankTitleLabel.setText("Top 3 Rank - Multiplayer");
    }

    @FXML
    public void onBackClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigat înapoi la meniul principal TicTacToe din clasament.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Eroare la încărcarea meniului principal TicTacToe: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Eroare de navigare", "Nu s-a putut reveni la meniul TicTacToe.");
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
