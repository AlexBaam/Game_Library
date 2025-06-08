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
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeJoinScreen {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button joinButton;

    @FXML
    public Button backButton;

    @FXML
    private TextField roomIDButton;

    @FXML
    public void OnJoinClick(ActionEvent event) {
        String roomId = roomIDButton.getText().trim().toUpperCase();

        if (roomId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Room ID", "Please enter a room ID before joining.");
            return;
        }

        try {
            ClientToServerProxy.send(List.of("tictactoe", "newgame", "join", roomId));
            String response = (String) ClientToServerProxy.receive();

            if (response.toLowerCase().startsWith("success")) {
                String mode = "network";
                String symbol = "O";

                for (String part : response.split(":|;")) {
                    if (part.startsWith("mode=")) {
                        mode = part.split("=")[1];
                    } else if (part.startsWith("symbol=")) {
                        symbol = part.split("=")[1];
                    }
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeBoard.fxml"));
                Parent root = loader.load();

                TicTacToeBoard controller = loader.getController();
                controller.setCurrentSymbol(symbol);
                controller.setMode(mode);
                controller.startListeningForUpdates();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TicTacToe - Multiplayer Game");
                stage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Join Failed", response);
            }

        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not join room: " + e.getMessage());
        }
    }

    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (TicTacToe - Join Screen)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeConnectionScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to Connection Screen.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: " + e.getMessage());
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
