package org.example.game_library.views.minesweeper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.views.tictactoe.TicTacToeBoard;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperBoard {
    private static final Logger logger = AppLogger.getLogger();

    private String mode;

    @FXML
    private Button forfeitButton;

    @FXML
    private Button saveButton;
    

    @FXML
    public void onSaveClick() {
        // Lasă gol pentru implementare viitoare
    }

    @FXML
    public void onForfeitClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed forfeit button.");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm forfeit...");
        confirm.setHeaderText("Are you sure you want to forfeit?");
        confirm.setContentText("Pressing yes will result in a lose!");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");

        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(choice -> {
            if (choice == yes) {
                try {
                    logger.log(Level.INFO, "User decided to forfeit the game.");

                    ClientToServerProxy.send(List.of("minesweeper", "forfeit"));

                    String response = (String) ClientToServerProxy.receive();

                    if ("SUCCESS".equals(response)) {
                        logger.log(Level.INFO, "Successfully forfeited the game.");

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml")
                        );

                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Minesweeper - New Game");
                        stage.show();
                    } else {
                        logger.log(Level.WARNING, "Failed to forfeit the game.");
                        logger.log(Level.WARNING, "Server response: {0}", response);
                    }
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Couldn't forfeit the game! Reason: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.log(Level.INFO, "User gave up on the forfeit of the game.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void returnToNewGameScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Minesweeper - New Game");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Couldn't go back to new game screen.");
        }
    }


    public void loadGameToUI(TicTacToeGame loadedGame) {

    }

    public void startListeningForUpdates() {

    }

    public void setMode(String mode) {
        this.mode = mode;
        // Poți folosi `mode` pentru a inițializa jocul, de exemplu numărul de mine, dimensiunea tablei, etc.
        System.out.println("Game mode set to: " + mode);
    }
}
