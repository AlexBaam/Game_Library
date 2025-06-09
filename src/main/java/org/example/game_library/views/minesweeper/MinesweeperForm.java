package org.example.game_library.views.minesweeper;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.minesweeper_game_logic.Cell;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    private GridPane boardGrid;
    @FXML
    private Label timerLabel;
    @FXML
    private Label minesRemainingLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button forfeitButton;

    private Button[][] uiCells;
    private Timeline gameTimer;
    private int timeElapsedInSeconds;
    private int totalMines;
    private Thread listenerThread;
    private final AtomicBoolean isListening = new AtomicBoolean(false);

    public void initializeGameUI(int rows, int cols, int totalMines) {
        this.totalMines = totalMines;
        minesRemainingLabel.setText("Mines: " + totalMines);

        setupBoardUI(rows, cols);
        startTimer();
        startListeningForServerUpdates();
    }

    private void setupBoardUI(int rows, int cols) {
        boardGrid.getChildren().clear();
        boardGrid.getRowConstraints().clear();
        boardGrid.getColumnConstraints().clear();

        uiCells = new Button[rows][cols];

        for (int i = 0; i < rows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(30);
            boardGrid.getRowConstraints().add(rc);
        }
        for (int i = 0; i < cols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(30);
            boardGrid.getColumnConstraints().add(cc);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button cellButton = new Button();
                cellButton.setMinSize(30, 30);
                cellButton.setMaxSize(30, 30);
                cellButton.getStyleClass().add("minesweeper-cell");

                final int row = r;
                final int intCol = c;

                cellButton.setOnMouseClicked(event -> handleCellClick(event, row, intCol));

                GridPane.setConstraints(cellButton, intCol, row);
                boardGrid.getChildren().add(cellButton);
                uiCells[r][intCol] = cellButton;
            }
        }
        boardGrid.setPrefWidth(cols * 30);
        boardGrid.setPrefHeight(rows * 30);
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        timeElapsedInSeconds = 0;
        timerLabel.setText("Time: 00:00");
        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    timeElapsedInSeconds++;
                    updateTimerLabel();
                })
        );
        gameTimer.setCycleCount(Animation.INDEFINITE);
        gameTimer.play();
    }

    private void updateTimerLabel() {
        int minutes = timeElapsedInSeconds / 60;
        int seconds = timeElapsedInSeconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    @FXML
    private void handleCellClick(MouseEvent event, int r, int c) {
        String clickType;
        if (event.getButton() == MouseButton.PRIMARY) {
            clickType = "primary";
        } else if (event.getButton() == MouseButton.SECONDARY) {
            clickType = "secondary";
        } else {
            return;
        }

        try {
            ClientToServerProxy.send(List.of("minesweeper", "click", String.valueOf(r), String.valueOf(c), clickType));
            logger.log(Level.INFO, "Sent minesweeper click to server: row={0}, col={1}, type={2}", new Object[]{r, c, clickType});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending click to server: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not send move to server: " + e.getMessage());
        }
    }

    private void startListeningForServerUpdates() {
        isListening.set(true);
        listenerThread = new Thread(() -> {
            try {
                while (isListening.get()) {
                    Object raw = ClientToServerProxy.receive();
                    if (raw == null) {
                        logger.log(Level.WARNING, "Received null from server. Listener might be closing or server disconnected.");
                        break;
                    }

                    Platform.runLater(() -> {
                        if (raw instanceof List<?> responseList) {
                            if (responseList.isEmpty()) {
                                return;
                            }

                            String status = responseList.get(0).toString();
                            int minesLeft = -1;
                            if (responseList.size() > 1 && responseList.get(1) instanceof Integer) {
                                minesLeft = (Integer) responseList.get(1);
                                minesRemainingLabel.setText("Mines: " + minesLeft);
                            } else {
                                logger.log(Level.WARNING, "Mines left count missing or malformed in server response: " + responseList);
                            }

                            List<Cell> updatedCells = new ArrayList<>();
                            if (responseList.size() > 2) {
                                try {
                                    for (int i = 2; i < responseList.size(); i++) {
                                        if (responseList.get(i) instanceof Cell) {
                                            updatedCells.add((Cell) responseList.get(i));
                                        } else {
                                            logger.log(Level.WARNING, "Non-Cell object found in updatedCells list: " + responseList.get(i).getClass().getName());
                                        }
                                    }
                                    updateBoardUI(updatedCells);
                                } catch (ClassCastException e) {
                                    logger.log(Level.SEVERE, "Received malformed cell update list: " + responseList.subList(2, responseList.size()), e);
                                }
                            }

                            if ("GAME_OVER".equals(status) || "GAME_WON".equals(status)) {
                                stopTimer();
                                String message = "Game Over!";
                                if ("GAME_WON".equals(status)) {
                                    message = "Congratulations! You won!";
                                }
                                showAlert(Alert.AlertType.INFORMATION, "Game Result", message);
                                returnToNewGameScreen();
                            }
                        } else if (raw instanceof String response) {
                            if (response.startsWith("FAILURE")) {
                                showAlert(Alert.AlertType.ERROR, "Server Error", response);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                if (isListening.get()) {
                    logger.log(Level.SEVERE, "Error during Minesweeper game updates listening.", e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Connection Lost", "Lost connection to server."));
                } else {
                    logger.log(Level.INFO, "Minesweeper listener thread stopped gracefully (socket closed).");
                }
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Received unknown object type from server.", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Server Error", "Received unexpected data type from server."));
            } finally {
                isListening.set(false);
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // In MinesweeperForm.java
    private void stopListeningForServerUpdates() {
        if (isListening.get()) {
            logger.log(Level.INFO, "Attempting to stop Minesweeper listener thread.");
            isListening.set(false);
            try {
                // This is the change: call close() instead of closeConnection()
                ClientToServerProxy.close();
                if (listenerThread != null && listenerThread.isAlive()) {
                    listenerThread.join(1000);
                    if (listenerThread.isAlive()) {
                        logger.log(Level.WARNING, "Minesweeper listener thread did not terminate gracefully within timeout. Interrupting...");
                        listenerThread.interrupt();
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket for Minesweeper listener: {0}", e.getMessage());
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for Minesweeper listener thread to stop.", e);
            }
        }
    }

    private void updateBoardUI(List<Cell> updatedCells) {
        for (Cell cell : updatedCells) {
            Button button = uiCells[cell.getRow()][cell.getCol()];
            button.getStyleClass().removeAll("revealed", "mine", "flagged",
                    "num-1", "num-2", "num-3", "num-4", "num-5", "num-6", "num-7", "num-8");

            if (cell.isRevealed()) {
                button.getStyleClass().add("revealed");
                if (cell.isMine()) {
                    button.getStyleClass().add("mine");
                    button.setText("");
                    button.setDisable(true);
                } else {
                    if (cell.getAdjacentMinesCount() > 0) {
                        button.setText(String.valueOf(cell.getAdjacentMinesCount()));
                        button.getStyleClass().add("num-" + cell.getAdjacentMinesCount());
                    } else {
                        button.setText("");
                    }
                    button.setDisable(true);
                }
            } else if (cell.isFlagged()) {
                button.getStyleClass().add("flagged");
                button.setText("");
                button.setDisable(false);
            } else {
                button.setText("");
                button.setDisable(false);
            }
        }
    }

    @FXML
    private void onSaveClick() {
        try {
            ClientToServerProxy.send(List.of("minesweeper", "save"));
            logger.log(Level.INFO, "Sent minesweeper save request to server.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending save request: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not send save request to server.");
        }
    }

    @FXML
    private void onForfeitClick(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Forfeit");
        confirm.setHeaderText("Are you sure you want to forfeit this game?");
        confirm.setContentText("Forfeiting will result in a loss for this game.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stopTimer();
            try {
                ClientToServerProxy.send(List.of("minesweeper", "forfeit"));
                logger.log(Level.INFO, "Sent minesweeper forfeit request to server.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending forfeit request: {0}", e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not send forfeit request to server.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void returnToNewGameScreen() {
        stopListeningForServerUpdates();

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) boardGrid.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Minesweeper - New Game");
                stage.show();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to return to Minesweeper New Game screen: " + e.getMessage(), e);
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to new game screen.");
            }
        });
    }

    @FXML
    private void onNewGameClick(ActionEvent event) {
        logger.log(Level.INFO, "New Game Minesweeper clicked from main menu. Navigating to difficulty selection.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Minesweeper - Select Difficulty");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load Minesweeper new game screen: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Minesweeper new game screen.");
        }
    }

    @FXML
    private void onLoadGameClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Load Game", "Funcționalitatea 'Load Game' Minesweeper nu este încă implementată.");
        logger.log(Level.INFO, "Load Game Minesweeper clicked.");
    }

    @FXML
    private void onScoreboardClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/scoreFormMinesweeper.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated to Minesweeper Scoreboard.");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading Minesweeper scoreboard: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not display Minesweeper scoreboard.");
        }
    }

    @FXML
    public void onBackClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed back button from Minesweeper main menu. Navigating to user dashboard.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/userDashboardForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.log(Level.INFO, "Navigated back to user dashboard.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load back screen (user dashboard): " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to main menu.");
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        logger.log(Level.INFO, "Exit Minesweeper from main menu clicked.");
        try {
            ClientToServerProxy.send(List.of("exit"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not send exit command to server: {0}", e.getMessage());
        } finally {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }
}