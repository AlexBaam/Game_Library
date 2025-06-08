package org.example.game_library.views.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeNewGameScreen {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public void onAIClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed vs AI button. (TicTacToe - New Game)");
        startGame("ai", event);
    }

    @FXML
    public void OnLocalClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed 'vs Local' button. (TicTacToe - New Game)");
        startGame("local", event);
    }

    @FXML
    public void OnPlayerClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed 'vs Player' button. (TicTacToe - New Game)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeConnectionScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated to Connection Screen.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: " + e.getMessage());
        }
    }

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (TicTacToe - New Game)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to main menu.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: " + e.getMessage());
        }
    }

    private void startGame(String modeRequested, ActionEvent event) {
        try {
            ClientToServerProxy.send(List.of("tictactoe", "newgame", modeRequested));
            String response = (String) ClientToServerProxy.receive();

            if (response.toLowerCase().startsWith("success")) {
                String actualMode = modeRequested;

                if (response.contains("mode=")) {
                    for (String part : response.split(":|;")) {
                        if (part.startsWith("mode=")) {
                            actualMode = part.split("=")[1];
                            break;
                        }
                    }
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeBoard.fxml"));
                Parent root = loader.load();

                TicTacToeBoard controller = loader.getController();
                controller.setMode(actualMode);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TicTacToe - " + actualMode.toUpperCase() + " Game");
                stage.show();

            } else {
                logger.log(Level.WARNING, "Server response: {0}", response);
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error during game initialization: {0}", e.getMessage());
        }
    }
}
