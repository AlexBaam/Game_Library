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
                        logger.log(Level.WARNING, "Received unexpected object type in score list: " + item.getClass().getName());
                        showAlert(Alert.AlertType.ERROR, "Eroare de date", "A apărut o eroare la interpretarea datelor de scor.");
                        return;
                    }
                }
                scoreTable.setItems(data);
                logger.log(Level.INFO, "Scorurile TicTacToe au fost încărcate cu succes. Număr de înregistrări: " + data.size());
            } else if (response instanceof String errorMessage) {
                showAlert(Alert.AlertType.ERROR, "Eroare Server", errorMessage);
                logger.log(Level.WARNING, "Eroare server la încărcarea scorurilor: " + errorMessage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Eroare comunicare", "Răspuns neașteptat de la server: " + response);
                logger.log(Level.WARNING, "Răspuns neașteptat de la server: " + (response != null ? response.getClass().getName() : "null"));
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare rețea", "Nu s-a putut conecta la server pentru a obține scorurile.");
            logger.log(Level.SEVERE, "Eroare de rețea la încărcarea scorurilor: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare protocol", "Probleme la deserializarea datelor de scor de la server.");
            logger.log(Level.SEVERE, "ClassNotFoundException la încărcarea scorurilor: " + e.getMessage());
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