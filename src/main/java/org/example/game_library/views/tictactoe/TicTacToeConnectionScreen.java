package org.example.game_library.views.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeConnectionScreen {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button. (TicTacToe - Connection Screen)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeNewGameScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to New Game Screen.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: {0}", e.getMessage());
        }
    }

    @FXML
    public void onJoinClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed Join Room button. (TicTacToe - Connection Screen)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeJoinScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated to Join Room Screen.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen: {0}", e.getMessage());
        }
    }

    @FXML
    public void onHostClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed host button. (TicTacToe - Connection Screen)");
        try {
            List<String> request = List.of("tictactoe", "newgame", "host");
            ClientToServerProxy.send(request);
            String response = (String) ClientToServerProxy.receive();

            if (response.startsWith("ROOM_ID:")) {
                String roomId = response.substring("ROOM_ID:".length());

                logger.log(Level.INFO, "Room hosted successfully with ID: {0}", roomId);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Room Created");
                alert.setHeaderText("Share this Room ID:");
                alert.setContentText(roomId);
                alert.showAndWait();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeBoard.fxml"));
                Parent root = loader.load();

                TicTacToeBoard controller = loader.getController();
                controller.setCurrentSymbol("X");
                controller.setMode("network");

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TicTacToe - Online Game");
                stage.show();

            } else {
                logger.log(Level.WARNING, "Unexpected server response: {0}", response);
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error during local game initialization: {0}", e.getMessage());
        }
    }
}
