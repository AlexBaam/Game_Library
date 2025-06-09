package org.example.game_library.views.minesweeper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.views.tictactoe.TicTacToeBoard;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperNewGameScreen {

        private static final Logger logger = AppLogger.getLogger();


    @FXML
    public void onEasyClick(ActionEvent event) {
        startGame("easy", event);
    }

    @FXML
    public void onMediumClick(ActionEvent event) {
        startGame("medium", event);
    }

    @FXML
    public void onHardClick(ActionEvent event) {
        startGame("hard", event);
    }


        @FXML
        public void onBackClick(ActionEvent event) {
            logger.log(Level.INFO, "User pressed back button. (Minesweeper - New Game)");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml"));
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
            ClientToServerProxy.send(List.of("minesweeper", "newgame", modeRequested));
            String response = (String) ClientToServerProxy.receive();

            if (response.toLowerCase().startsWith("success")) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperBoard.fxml"));
                Parent root = loader.load();

                MinesweeperBoard controller = loader.getController();
                controller.setMode(modeRequested);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Minesweeper - " + modeRequested.toUpperCase() + " Game");
                stage.show();

            } else {
                logger.log(Level.WARNING, "Server response: {0}", response);
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error during game initialization: {0}", e.getMessage());
        }
    }


}
