package org.example.game_library.views.minesweeper;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntryM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
// Nu mai e nevoie de CommandMinesweeper aici dacă nu îl folosești explicit
// import org.example.game_library.networking.enums.CommandMinesweeper;
//import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntry;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScoreFormMinesweeper {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button backButton;

    @FXML
    private TableView<ScoreEntryM> scoreTable;

    @FXML
    private TableColumn<ScoreEntryM, Integer> rankColumn;

    @FXML
    private TableColumn<ScoreEntryM, String> usernameColumn;

    @FXML
    private TableColumn<ScoreEntryM, Integer> totalScoreColumn;

    @FXML
    private Label rankTitleLabel;

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getRank()));
        usernameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUsername()));
        totalScoreColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalScore()));

        loadScores();
        rankTitleLabel.setText("Top Minesweeper Scores");

        // Aici este modificarea esențială:
        // Trimite comanda exact cum ai sugerat tu.
        // Apelăm fără parametru scoreType, deoarece este implicit
    }

    private void loadScores() {
        try {
            ClientToServerProxy.send(List.of("minesweeper", "score"));

            Object response = ClientToServerProxy.receive();

            if (response instanceof List<?> scoreList) {
                ObservableList<ScoreEntryM> data = FXCollections.observableArrayList();
                for (Object item : scoreList) {
                    if (item instanceof ScoreEntryM) {
                        data.add((ScoreEntryM) item);
                    } else {
                        logger.log(Level.WARNING, "Received unexpected object type in Minesweeper score list: " + item.getClass().getName());
                        showAlert(Alert.AlertType.ERROR, "Eroare de date", "A apărut o eroare la interpretarea datelor de scor Minesweeper.");
                        return;
                    }
                }

                for (ScoreEntryM entry : data) {
                    System.out.println("Rank: " + entry.getRank() + ", Username: " + entry.getUsername() + ", TotalScore: " + entry.getTotalScore());
               }

                scoreTable.setItems(data);
               System.out.println("Numar iteme in tabela: " + scoreTable.getItems().size());
                logger.log(Level.INFO, "Scorurile Minesweeper au fost încărcate cu succes. Număr de înregistrări: " + data.size());
            } else if (response instanceof String errorMessage) {
                showAlert(Alert.AlertType.ERROR, "Eroare Server", "Server error for Minesweeper scores: " + errorMessage);
                logger.log(Level.WARNING, "Server error when loading Minesweeper scores: " + errorMessage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Eroare comunicare", "Răspuns neașteptat de la server pentru clasamentul Minesweeper: " + (response != null ? response.getClass().getName() : "null"));
                logger.log(Level.WARNING, "Unexpected response from server for Minesweeper scores: " + (response != null ? response.getClass().getName() : "null"));
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare rețea", "Nu s-a putut conecta la server pentru a obține scorurile Minesweeper.");
            logger.log(Level.SEVERE, "Network error when loading Minesweeper scores: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare protocol", "Probleme la deserializarea datelor de scor Minesweeper de la server.");
            logger.log(Level.SEVERE, "ClassNotFoundException when loading Minesweeper scores: " + e.getMessage());
        }
    }

    @FXML
    public void onBackClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigat înapoi la meniul principal al utilizatorului.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Eroare la încărcarea meniului principal al utilizatorului: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Eroare de navigare", "Nu s-a putut reveni la meniul principal.");
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