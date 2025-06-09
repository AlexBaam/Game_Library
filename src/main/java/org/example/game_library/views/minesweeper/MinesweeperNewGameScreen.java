package org.example.game_library.views.minesweeper;

import org.example.game_library.utils.loggers.AppLogger;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.enums.DifficultyLevel;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperNewGameScreen {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public void onEasyClick(ActionEvent event) {
        logger.log(Level.INFO, "User selected Easy difficulty for Minesweeper.");
        startGame(DifficultyLevel.EASY, event);
    }

    @FXML
    public void onMediumClick(ActionEvent event) {
        logger.log(Level.INFO, "User selected Medium difficulty for Minesweeper.");
        startGame(DifficultyLevel.MEDIUM, event);
    }

    @FXML
    public void onHardClick(ActionEvent event) {
        logger.log(Level.INFO, "User selected Hard difficulty for Minesweeper.");
        startGame(DifficultyLevel.HARD, event);
    }

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button from Minesweeper New Game screen.");
        try {
            // Presupunem că te întorci la ecranul principal al jocurilor (userDashboardForm)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml")); // Revii la meniul principal Minesweeper, nu la dashboard
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Minesweeper Main Menu"); // Titlul ar trebui să fie mai generic
            stage.show();
            logger.log(Level.INFO, "Navigated back to Minesweeper main menu.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load Minesweeper main menu: " + e.getMessage(), e);
            // Poți adăuga un showAlert aici dacă vrei
        }
    }

    private void startGame(DifficultyLevel difficulty, ActionEvent event) {
        try {
            // Vom trimite serverului comanda de newgame cu dificultatea
            // Serverul va crea instanța MinesweeperGame și o va asocia cu sesiunea curentă
            ClientToServerProxy.send(List.of("minesweeper", "newgame", difficulty.name().toLowerCase()));

            // Așteptăm confirmarea de la server.
            // Pentru Minesweeper, serverul ar putea trimite înapoi dimensiunile tablei.
            Object responseObj = ClientToServerProxy.receive();
            if (responseObj instanceof String response && response.startsWith("SUCCESS")) {
                logger.log(Level.INFO, "Server responded to new Minesweeper game request: {0}", response);

                // Parsăm dimensiunile tablei din răspuns, dacă serverul le trimite
                // Exemplu: "SUCCESS;rows=9;cols=9;mines=10"
                int rows = 9, cols = 9, mines = 10; // Valori default, se vor actualiza
                String[] parts = response.split(";");
                for (String part : parts) {
                    if (part.startsWith("rows=")) rows = Integer.parseInt(part.substring(5));
                    else if (part.startsWith("cols=")) cols = Integer.parseInt(part.substring(5));
                    else if (part.startsWith("mines=")) mines = Integer.parseInt(part.substring(6));
                }

                // Incarcă FXML-ul pentru tabla de Minesweeper
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperBoard.fxml")); // Noul FXML pentru tabla de joc
                Parent root = loader.load();

                // Obține controllerul MinesweeperForm și inițializează-l cu parametrii jocului
                MinesweeperForm controller = loader.getController();
                // Vom adăuga o metodă initializeGameUI în MinesweeperForm pentru a seta grila vizuală
                controller.initializeGameUI(rows, cols, mines); // Acum pasezi și numărul de mine

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Minesweeper - " + difficulty.name() + " Game");
                stage.show();
            } else {
                logger.log(Level.WARNING, "Server failed to start new Minesweeper game: {0}", responseObj);
                // Aici poți afișa un Alert
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error during Minesweeper game initialization: {0}");
            // Aici poți afișa un Alert
        }
    }
}
