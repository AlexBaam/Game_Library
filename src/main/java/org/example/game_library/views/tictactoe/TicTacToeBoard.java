package org.example.game_library.views.tictactoe;

import javafx.application.Platform;
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
import lombok.Setter;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.ui.ShowAlert;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeBoard {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button cell1;

    @FXML
    public Button cell2;

    @FXML
    public Button cell3;

    @FXML
    public Button cell4;

    @FXML
    public Button cell5;

    @FXML
    public Button cell6;

    @FXML
    public Button cell7;

    @FXML
    public Button cell8;

    @FXML
    public Button cell9;

    @FXML
    public Button saveButton;

    @FXML
    public Button forfeitButton;

    @FXML
    private GridPane boardGrid;

    @Setter
    private String currentSymbol = "X";

    @Setter
    private String mode;

    @Setter
    private Stage stage;

    private static final String TICTACTOE = "tictactoe";
    private static final String NEWGAMEPATH = "/org/example/game_library/FXML/tictactoe/tictactoeNewGameScreen.fxml";

    private void togglePlayer() {
        currentSymbol = currentSymbol.equals("X") ? "O" : "X";
    }

    @FXML
    public void handleCellClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        Integer row = GridPane.getRowIndex(clicked);
        Integer col = GridPane.getColumnIndex(clicked);
        if (row == null) row = 0;
        if (col == null) col = 0;

        try {
            ClientToServerProxy.send(List.of(TICTACTOE, "move", row.toString(), col.toString(), currentSymbol));

            clicked.setText(currentSymbol);
            clicked.setDisable(true);

            if ("network".equalsIgnoreCase(mode)) {
                return;
            }

            boolean done = false;
            while (!done) {
                Object raw = ClientToServerProxy.receive();
                if (!(raw instanceof String)){
                    continue;
                }

                String response = (String) raw;

                if (response.startsWith("AI_MOVE:")) {
                    logger.log(Level.INFO, response);
                    String[] coords = response.substring(8).split(",");
                    int aiRow = Integer.parseInt(coords[0]);
                    int aiCol = Integer.parseInt(coords[1]);
                    Button aiCell = getButtonAt(aiRow, aiCol);
                    aiCell.setText("O");
                    aiCell.setDisable(true);
                } else if (response.startsWith("WIN:")) {
                    logger.log(Level.INFO, response);
                    ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Game Over", "Player " + currentSymbol + " wins!");
                    returnToNewGameScreen(event);
                    done = true;
                } else if (response.startsWith("LOSE:")) {
                    logger.log(Level.INFO, response);
                    ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Game Over", "You lost!");
                    returnToNewGameScreen(event);
                    done = true;
                } else if (response.equalsIgnoreCase("DRAW!")) {
                    logger.log(Level.INFO, response);
                    ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Game Over", "It's a draw!");
                    returnToNewGameScreen(event);
                    done = true;
                } else if (response.equalsIgnoreCase("SUCCESS")) {
                    logger.log(Level.INFO, response);
                    if ("local".equalsIgnoreCase(mode)) togglePlayer();
                    done = true;
                } else if (response.startsWith("FAILURE")) {
                    logger.log(Level.INFO, response);
                    ShowAlert.showAlert(Alert.AlertType.WARNING, "Invalid move", response);
                    done = true;
                } else {
                    logger.log(Level.WARNING, "Unhandled: {0}", response);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Connection Error", "Lost connection to server: " + e.getMessage());
            logger.log(Level.SEVERE, "Lost connection to server: {0}", e.getMessage());
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Unexpected Error", e.getMessage());
            logger.log(Level.SEVERE, "Unexpected Error: {0}", e.getMessage());
        }
    }

    private Button getButtonAt(int oppRow, int oppCol) {
        for (Node node : boardGrid.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);

            if (row == null) row = 0;
            if (col == null) col = 0;

            if (row == oppRow && col == oppCol && node instanceof Button button) {
                return button;
            }
        }
        return null;
    }

    @FXML
    public void onSaveClick() {
        try {
            ClientToServerProxy.send(List.of(TICTACTOE, "save"));
            String response = (String) ClientToServerProxy.receive();

            if ("SUCCESS".equals(response)) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Game Saved", "Your game was saved successfully!");
            } else {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Save Failed", response);
            }
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Error", "Could not save game: " + e.getMessage());
        }
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

                    ClientToServerProxy.send(List.of(TICTACTOE, "forfeit"));

                    String response =  (String) ClientToServerProxy.receive();

                    if ("SUCCESS".equals(response)) {
                        logger.log(Level.INFO, "Successfully forfeited the game.");

                        FXMLLoader loader = new FXMLLoader(
                                getClass().
                                    getResource(NEWGAMEPATH)
                        );

                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("TicTacToe - New Game");
                        stage.show();
                    } else {
                        logger.log(Level.WARNING, "Failed to forfeit the game.");
                        logger.log(Level.WARNING, "Server response: {0}", response);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "Error", "Couldn't forfeit the game! Reason: " + e.getMessage());
                }
            } else {
                logger.log(Level.INFO, "User gave up on the forfeit of the game.");
            }
        });
    }

    private void returnToNewGameScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(NEWGAMEPATH));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TicTacToe - New Game");
            stage.show();
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Couldn't go back to new game screen! Reason: " + e.getMessage());
        }
    }

    private void returnToNewGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(NEWGAMEPATH));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("TicTacToe - New Game");
            stage.show();
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Couldn't go back to new game screen! Reason: " + e.getMessage());
        }
    }

    public void loadGameToUI(TicTacToeGame loadedGame) {
        String[][] board = loadedGame.getBoard();

        for (Node node : boardGrid.getChildren()) {
            if (node instanceof Button button) {
                Integer row = GridPane.getRowIndex(button);
                Integer col = GridPane.getColumnIndex(button);

                if (row == null) row = 0;
                if (col == null) col = 0;

                String cellValue = board[row][col];
                button.setText(cellValue);
                button.setDisable(!cellValue.equals(" "));
            }
        }

        this.currentSymbol = loadedGame.getCurrentSymbol();
    }

    public void startListeningForUpdates() {
        if (!"network".equalsIgnoreCase(mode)){
            return;
        }

        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    Object raw = ClientToServerProxy.receive();
                    if (!(raw instanceof String message)){
                        continue;
                    }

                    if (message.startsWith("OPPONENT_MOVED:")) {
                        String[] coords = message.substring("OPPONENT_MOVED:".length()).split(",");
                        int row = Integer.parseInt(coords[0]);
                        int col = Integer.parseInt(coords[1]);

                        Platform.runLater(() -> {
                            Button cell = getButtonAt(row, col);
                            if (cell != null) {
                                cell.setText(currentSymbol.equals("X") ? "O" : "X");
                                cell.setDisable(true);
                            }
                        });

                    } else if (message.startsWith("WIN:") || message.startsWith("LOSE:") || message.equals("DRAW!")) {
                        Platform.runLater(() -> {
                            ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Game Over", message);
                            returnToNewGameScreen();
                        });
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

}
