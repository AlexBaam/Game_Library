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
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.minesweeper_game_logic.Cell;
import org.example.game_library.networking.server.minesweeper_game_logic.MinesweeperGameState;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperBoard {
    private static final Logger logger = AppLogger.getLogger();

    private MinesweeperGameState currentGameState;
    private int rows;
    private int cols;
    private int totalMines;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private Button[][] buttonMatrix;

    @FXML
    private Button forfeitButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button flagButton;
    @FXML
    private Button shovelButton;
    @FXML
    private GridPane boardGrid;
    @FXML
    private Label timerLabel;
    @FXML
    private Label minesRemainingLabel;

    @FXML
    public void initialize() {
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(1);
        boardGrid.setVgap(1);
        timerLabel.setText("Time: 00:00");
        minesRemainingLabel.setText("Mines: 0");

        if (shovelButton != null) shovelButton.setDisable(true);
        if (flagButton != null) flagButton.setDisable(true);
    }

    public void setInitialGameState(MinesweeperGameState gameState) {
        this.currentGameState = gameState;
        this.rows = gameState.getRows();
        this.cols = gameState.getCols();
        this.totalMines = gameState.getMineCount();
        populateBoardUI();
        updateMineCountLabel();
    }


    private void populateBoardUI() {
        boardGrid.getChildren().clear();
        boardGrid.getRowConstraints().clear();
        boardGrid.getColumnConstraints().clear();
        buttonMatrix = new Button[rows][cols];

        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / rows);
            boardGrid.getRowConstraints().add(rowConstraints);
        }

        for (int j = 0; j < cols; j++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cols);
            boardGrid.getColumnConstraints().add(colConstraints);
        }

        double maxBoardSize = 480;
        boardGrid.setMaxWidth(400);
        boardGrid.setMaxHeight(400);
        boardGrid.setPrefSize(400, 400);

        double cellWidth = maxBoardSize / cols;
        double cellHeight = maxBoardSize / rows;
        double cellSize = Math.min(cellWidth, cellHeight);


        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button cellButton = new Button();
                cellButton.setPrefSize(cellSize, cellSize);
                cellButton.setMinSize(cellSize, cellSize);
                cellButton.setMaxSize(cellSize, cellSize);
                cellButton.getStyleClass().add("minesweeper-cell");

                final int r = row;
                final int c = col;
                cellButton.setOnMouseClicked(event -> {
                    if (selectedRow != -1 && selectedCol != -1) {
                        Button prevSelectedButton = buttonMatrix[selectedRow][selectedCol];
                        if (prevSelectedButton != null) {
                            prevSelectedButton.getStyleClass().remove("selected-cell");
                        }
                    }
                    selectedRow = r;
                    selectedCol = c;
                    cellButton.getStyleClass().add("selected-cell");

                    if (shovelButton != null) shovelButton.setDisable(false);
                    if (flagButton != null) flagButton.setDisable(false);

                    logger.log(Level.INFO, "Cell selected: ({0}, {1})", new Object[]{selectedRow, selectedCol});
                });

                buttonMatrix[row][col] = cellButton;
                boardGrid.add(cellButton, col, row);
            }
        }

        updateBoardUI();
        Platform.runLater(() -> boardGrid.requestLayout());
    }


    private void onCellClick(int row, int col) {
        logger.log(Level.INFO, "Cell clicked (reveal): ({0}, {1})", new Object[]{row, col});
        try {
            ClientToServerProxy.send(List.of("minesweeper", "reveal", String.valueOf(row), String.valueOf(col)));

            Object receivedGameState = ClientToServerProxy.receive();
            Object receivedStatus = ClientToServerProxy.receive();

            if (receivedGameState instanceof MinesweeperGameState newGameState && receivedStatus instanceof String statusMessage) {
                currentGameState = newGameState;
                updateBoardUI();
                Platform.runLater(() -> boardGrid.requestLayout());

                if (statusMessage.contains("game over")) {
                    showAlert(Alert.AlertType.INFORMATION, "Game Over!", "You hit a mine! Game lost.");
                    disableBoard();
                    revealAllMinesAtGameOver();
                } else if (isGameWon()) {
                    showAlert(Alert.AlertType.INFORMATION, "Congratulations!", "You cleared the minefield! Game won.");
                    disableBoard();
                }
            } else {
                logger.log(Level.WARNING, "Received unexpected object type for reveal. State: {0}, Status: {1}",
                        new Object[]{receivedGameState != null ? receivedGameState.getClass().getName() : "null",
                                receivedStatus != null ? receivedStatus.getClass().getName() : "null"});
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update game state after reveal. Please check server logs.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not reveal cell: " + e.getMessage());
            logger.log(Level.SEVERE, "Error revealing cell: {0}", e.getMessage());
        }
    }

    private void onCellRightClick(int row, int col) {
        logger.log(Level.INFO, "Cell right-clicked (flag): ({0}, {1})", new Object[]{row, col});
        try {
            ClientToServerProxy.send(List.of("minesweeper", "flag", String.valueOf(row), String.valueOf(col)));

            Object receivedGameState = ClientToServerProxy.receive();
            Object receivedStatus = ClientToServerProxy.receive();

            if (receivedGameState instanceof MinesweeperGameState newGameState && receivedStatus instanceof String statusMessage) {
                currentGameState = newGameState;
                updateBoardUI();
                updateMineCountLabel();
            } else {
                logger.log(Level.WARNING, "Received unexpected object type for flag. State: {0}, Status: {1}",
                        new Object[]{receivedGameState != null ? receivedGameState.getClass().getName() : "null",
                                receivedStatus != null ? receivedStatus.getClass().getName() : "null"});
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update game state after flagging. Please check server logs.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not flag cell: " + e.getMessage());
            logger.log(Level.SEVERE, "Error flagging cell: {0}", e.getMessage());
        }
    }


    @FXML
    public void onSaveClick() {
        logger.log(Level.INFO, "Save button clicked.");
        showAlert(Alert.AlertType.INFORMATION, "Save Game", "Game saving is not yet implemented.");
    }

    @FXML
    public void onForfeitClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed forfeit button.");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm forfeit...");
        confirm.setHeaderText("Are you sure you want to forfeit?");
        confirm.setContentText("Pressing yes will result in a lose!");

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) {
                try {
                    logger.log(Level.INFO, "User decided to forfeit the game.");

                    ClientToServerProxy.send(List.of("minesweeper", "forfeit"));

                    Object receivedObject = ClientToServerProxy.receive();
                    String response;

                    if (receivedObject instanceof String statusResponse) {
                        response = statusResponse;
                    } else {
                        response = "ERROR: Unexpected response type from server: " + (receivedObject != null ? receivedObject.getClass().getName() : "null");
                        logger.log(Level.SEVERE, response);
                    }

                    if ("SUCCESS".equals(response)) {
                        logger.log(Level.INFO, "Successfully forfeited the game.");
                        showAlert(Alert.AlertType.INFORMATION, "Game Forfeited", "You have forfeited the game.");
                        returnToNewGameScreen(event);
                    } else {
                        logger.log(Level.WARNING, "Failed to forfeit the game. Server response: {0}", response);
                        showAlert(Alert.AlertType.ERROR, "Forfeit Failed", "Failed to forfeit the game: " + response);
                    }
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Couldn't forfeit the game! Reason: " + e.getMessage());
                    logger.log(Level.SEVERE, "IOException during forfeit: {0}", e.getMessage());
                } catch (ClassNotFoundException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to process server response after forfeit: " + e.getMessage());
                    logger.log(Level.SEVERE, "ClassNotFoundException during deserialization after forfeit: {0}", e.getMessage());
                }
            } else {
                logger.log(Level.INFO, "User gave up on the forfeit of the game.");
            }
        });
    }

    @FXML
    public void onShovelClick(ActionEvent event) {
        if (selectedRow != -1 && selectedCol != -1) {
            logger.log(Level.INFO, "Shovel button clicked for cell: ({0}, {1})", new Object[]{selectedRow, selectedCol});
            try {
                ClientToServerProxy.send(List.of("minesweeper", "shovel", String.valueOf(selectedRow), String.valueOf(selectedCol)));

                Object receivedGameState = ClientToServerProxy.receive();
                Object receivedStatus = ClientToServerProxy.receive();

                if (receivedGameState instanceof MinesweeperGameState newGameState && receivedStatus instanceof String statusMessage) {
                    currentGameState = newGameState;
                    updateBoardUI();
                    Platform.runLater(() -> boardGrid.requestLayout());
                    updateMineCountLabel();

                    if (statusMessage.contains("game over")) {
                        showAlert(Alert.AlertType.INFORMATION, "Game Over!", "You hit a mine! Game lost.");
                        disableBoard();
                        revealAllMinesAtGameOver();
                    } else if (isGameWon()) {
                        showAlert(Alert.AlertType.INFORMATION, "Congratulations!", "You cleared the minefield! Game won.");
                        disableBoard();
                    }
                } else {
                    logger.log(Level.WARNING, "Received unexpected object type for shovel. State: {0}, Status: {1}",
                            new Object[]{receivedGameState != null ? receivedGameState.getClass().getName() : "null",
                                    receivedStatus != null ? receivedStatus.getClass().getName() : "null"});
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update game state after shovel. Please check server logs.");
                }
                deselectCell();
            } catch (IOException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Network Error", "Could not reveal cell with shovel: " + e.getMessage());
                logger.log(Level.SEVERE, "Error revealing cell with shovel: {0}", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No cell selected", "Please select a cell on the board first.");
            logger.log(Level.WARNING, "Shovel button clicked without a selected cell.");
        }
    }

    @FXML
    public void onFlagClick(ActionEvent event) {
        if (selectedRow != -1 && selectedCol != -1) {
            logger.log(Level.INFO, "Flag button clicked for cell: ({0}, {1})", new Object[]{selectedRow, selectedCol});
            try {
                ClientToServerProxy.send(List.of("minesweeper", "flag", String.valueOf(selectedRow), String.valueOf(selectedCol)));

                Object receivedGameState = ClientToServerProxy.receive();
                Object receivedStatus = ClientToServerProxy.receive();

                if (receivedGameState instanceof MinesweeperGameState newGameState && receivedStatus instanceof String statusMessage) {
                    currentGameState = newGameState;
                    updateBoardUI();
                    Platform.runLater(() -> boardGrid.requestLayout());
                    updateMineCountLabel();
                } else {
                    logger.log(Level.WARNING, "Received unexpected object type for flag. State: {0}, Status: {1}",
                            new Object[]{receivedGameState != null ? receivedGameState.getClass().getName() : "null",
                                    receivedStatus != null ? receivedStatus.getClass().getName() : "null"});
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update game state after flagging. Please check server logs.");
                }
                deselectCell();
            } catch (IOException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Network Error", "Could not flag cell: " + e.getMessage());
                logger.log(Level.SEVERE, "Error flagging cell: {0}", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No cell selected", "Please select a cell on the board first.");
            logger.log(Level.WARNING, "Flag button clicked without a selected cell.");
        }
    }

    private void deselectCell() {
        if (selectedRow != -1 && selectedCol != -1) {
            Button prevSelectedButton = buttonMatrix[selectedRow][selectedCol];
            if (prevSelectedButton != null) {
                prevSelectedButton.getStyleClass().remove("selected-cell");
            }
            selectedRow = -1;
            selectedCol = -1;
            if (shovelButton != null) shovelButton.setDisable(true);
            if (flagButton != null) flagButton.setDisable(true);
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

    public void updateBoardUI() {
        if (currentGameState == null) {
            return;
        }

        Cell[][] board = currentGameState.getBoard();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button cellButton = buttonMatrix[row][col]; // FOLOSEȘTI MATRICEA !!!
                Cell cell = board[row][col];

                cellButton.getStyleClass().removeAll("flagged", "mine", "revealed-empty", "selected-cell");
                for (int i = 1; i <= 8; i++) {
                    cellButton.getStyleClass().remove("number" + i);
                }
                cellButton.setText("");

                if (cell.isRevealed()) {
                    cellButton.setDisable(true);
                    if (cell.hasMine()) {
                        cellButton.getStyleClass().add("mine");
                        cellButton.setText("💣");
                    } else {
                        int adjacentMines = cell.getAdjacentMines();
                        if (adjacentMines > 0) {
                            cellButton.setText(String.valueOf(adjacentMines));
                            cellButton.getStyleClass().add("number" + adjacentMines);
                        } else {
                            cellButton.getStyleClass().add("revealed-empty");
                        }
                    }
                } else if (cell.isFlagged()) {
                    cellButton.setText("🚩");
                    cellButton.getStyleClass().add("flagged");
                    cellButton.setDisable(false);
                } else {
                    cellButton.setDisable(false);
                }

                if (row == selectedRow && col == selectedCol) {
                    cellButton.getStyleClass().add("selected-cell");
                }
            }
        }

        // Forțează redesenarea UI
        Platform.runLater(() -> boardGrid.requestLayout());
    }


    private void revealAllMinesAtGameOver() {
        if (currentGameState == null) return;
        Cell[][] board = currentGameState.getBoard();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = board[row][col];
                Button cellButton = buttonMatrix[row][col];
                if (cell.hasMine()) {
                    cellButton.getStyleClass().add("mine");
                    cellButton.setText("💣");
                }
                cellButton.setDisable(true);
            }
        }
    }

    private void updateMineCountLabel() {
        if (currentGameState != null) {
            int flaggedCells = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (currentGameState.getCell(r, c) != null && currentGameState.getCell(r, c).isFlagged()) {
                        flaggedCells++;
                    }
                }
            }
            minesRemainingLabel.setText("Mines: " + (totalMines - flaggedCells));
        }
    }

    private boolean isGameWon() {
        if (currentGameState == null) return false;

        Cell[][] board = currentGameState.getBoard();
        int revealedNonMineCells = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board[r][c];
                if (cell.isRevealed() && !cell.hasMine()) {
                    revealedNonMineCells++;
                }
            }
        }
        return revealedNonMineCells == (rows * cols - totalMines);
    }

    private void disableBoard() {
        for (Node node : boardGrid.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setDisable(true);
            }
        }
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
            logger.log(Level.SEVERE, "Error navigating back to new game screen: {0}", e.getMessage());
        }
    }
}