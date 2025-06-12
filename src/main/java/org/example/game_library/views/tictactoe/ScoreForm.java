package org.example.game_library.views.tictactoe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.tictactoe_game_logic.ScoreEntry;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.game_library.utils.ui.ShowAlert.showAlert;

public class ScoreForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    private TableView<ScoreEntry> scoreTable;

    @FXML
    private TableColumn<ScoreEntry, Integer> rankColumn;

    @FXML
    private TableColumn<ScoreEntry, String> usernameColumn;

    @FXML
    private TableColumn<ScoreEntry, Integer> winsColumn;

    @FXML
    private Button prevRankButton;

    @FXML
    private Button nextRankButton;

    @FXML
    private Label rankTitleLabel;

    @FXML
    private Button backButton;

    private boolean showingMultiplayerScores = true;

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        loadScores("network_wins");
        rankTitleLabel.setText("Top 3 Rank - Multiplayer");
    }

    private void loadScores(String scoreType) {
        try {
            ClientToServerProxy.send(List.of("tictactoe", "score", scoreType));

            Object response = ClientToServerProxy.receive();

            if (response instanceof List<?> scoreList) {
                ObservableList<ScoreEntry> data = FXCollections.observableArrayList();
                for (Object item : scoreList) {
                    if (item instanceof ScoreEntry) {
                        data.add((ScoreEntry) item);
                    } else {
                        logger.log(Level.WARNING, "Received unexpected object type in score list: {0}", item.getClass().getName());
                        showAlert(Alert.AlertType.ERROR, "Eroare de date", "A apărut o eroare la interpretarea datelor de scor.");
                        return;
                    }
                }
                scoreTable.setItems(data);
                logger.log(Level.INFO, "TicTacToe scores have been successfully uploaded. Number of records: {0}", data.size());
            } else if (response instanceof String errorMessage) {
                showAlert(Alert.AlertType.ERROR, "Eroare Server", errorMessage);
                logger.log(Level.WARNING, "Server error loading scores: {0}", errorMessage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Eroare comunicare", "Unexpected response from server: " + response);
                logger.log(Level.WARNING, "Unexpected response from server: {0}", (response != null ? response.getClass().getName() : "null"));
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Network error", "Could not connect to the server to get the scores.");
            logger.log(Level.SEVERE, "Network error loading scores: {0}", e.getMessage());
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare protocol", "Problems deserializing score data from the server.");
            logger.log(Level.SEVERE, "ClassNotFoundException when loading scores: {0}", e.getMessage());
        }
    }

    @FXML
    private void onPrevRankClick(ActionEvent event) {
        if (!showingMultiplayerScores) {
            loadScores("network_wins");
            rankTitleLabel.setText("Top 3 Rank - Multiplayer");
            showingMultiplayerScores = true;
        }
    }

    @FXML
    private void onNextRankClick(ActionEvent event) {
        if (showingMultiplayerScores) {
            loadScores("ai_wins");
            rankTitleLabel.setText("Top 3 Rank - AI");
            showingMultiplayerScores = false;
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to the main TicTacToe menu from the leaderboard.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading TicTacToe main menu: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation error", "Could not return to TicTacToe menu.");
        }
    }


}