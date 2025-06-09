package org.example.game_library.views.minesweeper;

import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform; // Import pentru a rula pe UI thread
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
import javafx.scene.input.MouseButton; // Pentru a distinge click-urile
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.networking.server.minesweeper_game_logic.Cell; // Importă clasa Cell
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinesweeperForm { // Am redenumit această clasă în MinesweeperForm, dacă nu era deja așa
    private static final Logger logger = AppLogger.getLogger();

    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private Thread listenerThread;

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

    private Button[][] uiCells; // Matrice pentru a stoca referințele butoanelor UI
    private Timeline gameTimer;
    private int timeElapsedInSeconds;
    private int totalMines; // Numărul total de mine, primit de la server
    private int flaggedMinesCount; // Numărul de steaguri plasate de utilizator

    public void initializeGameUI(int rows, int cols, int totalMines) {
        this.totalMines = totalMines;
        this.flaggedMinesCount = 0; // Resetăm contorul de steaguri
        minesRemainingLabel.setText("Mines: " + (totalMines - flaggedMinesCount));

        setupBoardUI(rows, cols);
        startTimer();

        // Asigură-te că oprești orice thread de ascultare anterior
        stopListeningForServerUpdates();
        // Apoi pornești unul nou
        startListeningForServerUpdates();
    }

    private void setupBoardUI(int rows, int cols) {
        boardGrid.getChildren().clear(); // Curăță orice butoane vechi
        boardGrid.getRowConstraints().clear();
        boardGrid.getColumnConstraints().clear();

        uiCells = new Button[rows][cols];

        // Adaugă constrângeri dinamice pentru rânduri și coloane
        for (int i = 0; i < rows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(30); // Înălțimea preferată a celulei
            boardGrid.getRowConstraints().add(rc);
        }
        for (int i = 0; i < cols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(30); // Lățimea preferată a celulei
            boardGrid.getColumnConstraints().add(cc);
        }

        // Creează și adaugă butoanele în GridPane
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button cellButton = new Button();
                cellButton.setMinSize(30, 30); // Dimensiunea minimă a celulei
                cellButton.setMaxSize(30, 30); // Dimensiunea maximă a celulei
                cellButton.getStyleClass().add("minesweeper-cell"); // Adaugă clasa CSS
                // Nu seta textul inițial, e acoperită

                final int row = r; // Variabile finale pentru lambda
                final int col = c;

                cellButton.setOnMouseClicked(event -> handleCellClick(event, row, col));

                GridPane.setConstraints(cellButton, col, row); // Col, Row
                boardGrid.getChildren().add(cellButton);
                uiCells[r][c] = cellButton;
            }
        }
        // Ajustează dimensiunea GridPane-ului pentru a centra
        boardGrid.setPrefWidth(cols * 30);
        boardGrid.setPrefHeight(rows * 30);
        boardGrid.setLayoutX((boardGrid.getParent().getBoundsInLocal().getWidth() - boardGrid.getPrefWidth()) / 2);
        boardGrid.setLayoutY((boardGrid.getParent().getBoundsInLocal().getHeight() - boardGrid.getPrefHeight()) / 2);
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
            return; // Nu procesăm alte tipuri de click
        }

        try {
            // Trimiterea comenzii către server
            ClientToServerProxy.send(List.of("minesweeper", "click", String.valueOf(r), String.valueOf(c), clickType));
            logger.log(Level.INFO, "Sent minesweeper click to server: row={0}, col={1}, type={2}", new Object[]{r, c, clickType});

            // Răspunsul de la server va fi gestionat de startListeningForServerUpdates
            // Aici nu mai așteptăm un răspuns sincron, deoarece avem un thread dedicat.

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending click to server: {0}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not send move to server: " + e.getMessage());
        }
    }

    // Metoda pentru a porni thread-ul de ascultare
    private void startListeningForServerUpdates() {
        // Dacă există deja un thread care rulează, oprește-l
        stopListeningForServerUpdates();

        isListening.set(true); // Setăm flag-ul la true pentru a indica că thread-ul ar trebui să ruleze
        listenerThread = new Thread(() -> {
            try {
                while (isListening.get()) { // Citim starea AtomicBoolean
                    Object raw = ClientToServerProxy.receive();
                    if (raw == null) {
                        logger.log(Level.WARNING, "Received null from server. Listener might be closing.");
                        // Nu continuăm, ieșim dacă nu mai primim nimic.
                        break; // Ieșim din buclă dacă primim null
                    }

                    if (raw instanceof List<?> responseList) {
                        if (responseList.isEmpty()) {
                            continue;
                        }

                        String command = responseList.get(0).toString();

                        Platform.runLater(() -> {
                            if (!isListening.get()) { // Verificăm din nou dacă thread-ul ar trebui să se oprească
                                return; // Nu mai procesăm dacă am semnalat oprirea
                            }
                            if ("BOARD_UPDATE".equals(command) && responseList.size() > 1) {
                                try {
                                    @SuppressWarnings("unchecked")
                                    List<Cell> updatedCells = (List<Cell>) responseList.subList(1, responseList.size());
                                    updateBoardUI(updatedCells);
                                } catch (ClassCastException e) {
                                    logger.log(Level.SEVERE, "Received malformed board update: " + responseList, e);
                                }
                            } else if ("GAME_OVER".equals(command) || "GAME_WON".equals(command)) {
                                stopTimer();
                                String message = "Game Over!";
                                if ("GAME_WON".equals(command)) {
                                    message = "Congratulations! You won!";
                                }
                                showAlert(Alert.AlertType.INFORMATION, "Game Result", message);
                                stopListeningForServerUpdates(); // Oprim thread-ul explicit
                                returnToNewGameScreen();
                            } else if ("MINES_LEFT".equals(command) && responseList.size() > 1) {
                                try {
                                    int minesLeft = Integer.parseInt(responseList.get(1).toString());
                                    minesRemainingLabel.setText("Mines: " + minesLeft);
                                } catch (NumberFormatException e) {
                                    logger.log(Level.WARNING, "Invalid mines left count received: " + responseList.get(1), e);
                                }
                            } else{
                                showAlert(Alert.AlertType.ERROR, "Server Error", command); // command e de fapt mesajul de eroare
                            }
                        });
                    } else if (raw instanceof String response) {
                        Platform.runLater(() -> {
                            if (!isListening.get()) {
                                return;
                            }
                            if (response.startsWith("FAILURE")) {
                                showAlert(Alert.AlertType.ERROR, "Server Error", response);
                            }
                            // Alte mesaje simple de la server
                        });
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isListening.get()) { // Loghează doar dacă thread-ul nu a fost oprit intenționat
                    logger.log(Level.SEVERE, "Error during Minesweeper game updates listening.", e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Connection Lost", "Lost connection to server."));
                } else {
                    logger.log(Level.INFO, "Minesweeper listener thread stopped gracefully.");
                }
            } finally {
                isListening.set(false); // Asigură-te că flag-ul e false la ieșirea din buclă
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void stopListeningForServerUpdates() {
        if (isListening.get()) { // Verifică dacă thread-ul este activ
            logger.log(Level.INFO, "Attempting to stop Minesweeper listener thread.");
            isListening.set(false); // Semnalizează thread-ului să se oprească
            // Nu apelăm listenerThread.interrupt() direct, deoarece readObject poate fi blocat.
            // Serverul trebuie să închidă socket-ul pentru a debloca readObject.
            // Dacă clientul ar trebui să poată închide socketul unilateral, e o altă discuție.
        }
    }

    // Această metodă va actualiza UI-ul pe baza datelor primite de la server
    // Va parcurge doar celulele care au fost actualizate
    private void updateBoardUI(List<Cell> updatedCells) {
        for (Cell cell : updatedCells) {
            Button button = uiCells[cell.getRow()][cell.getCol()];
            // Curăță toate stilurile dinamice
            button.getStyleClass().removeAll("revealed", "mine", "flagged",
                    "num-1", "num-2", "num-3", "num-4", "num-5", "num-6", "num-7", "num-8");

            if (cell.isRevealed()) {
                button.getStyleClass().add("revealed");
                if (cell.isMine()) {
                    button.getStyleClass().add("mine");
                    button.setText(""); // Aici poți seta o imagine cu mina
                    button.setDisable(true); // O mină descoperită e dezactivată
                } else {
                    if (cell.getAdjacentMinesCount() > 0) {
                        button.setText(String.valueOf(cell.getAdjacentMinesCount()));
                        button.getStyleClass().add("num-" + cell.getAdjacentMinesCount());
                    } else {
                        button.setText(""); // Celulă goală (0 mine adiacente)
                    }
                    button.setDisable(true); // O celulă descoperită e dezactivată
                }
            } else if (cell.isFlagged()) {
                button.getStyleClass().add("flagged");
                button.setText(""); // Aici poți seta o imagine cu un steag
                button.setDisable(false); // Poți da click din nou pentru a scoate steagul
            } else {
                button.setText(""); // Celula acoperită
                button.setDisable(false); // Este activă pentru click
            }
        }
    }


    // Metode pentru butoanele Save și Forfeit
    @FXML
    private void onSaveClick() {
        // Logica pentru salvarea jocului (va veni mai târziu, dar trimitem o comandă generică acum)
        try {
            ClientToServerProxy.send(List.of("minesweeper", "save"));
            logger.log(Level.INFO, "Sent minesweeper save request to server.");
            // Răspunsul va fi gestionat de startListeningForServerUpdates
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
            stopTimer(); // Oprește timerul
            try {
                ClientToServerProxy.send(List.of("minesweeper", "forfeit"));
                logger.log(Level.INFO, "Sent minesweeper forfeit request to server.");
                // Serverul va trimite GAME_OVER, care va fi gestionat de listener
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending forfeit request: {0}", e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not send forfeit request to server.");
            }
        }
    }


    // Metodă ajutătoare pentru a afișa alerte
    private void showAlert(Alert.AlertType type, String title, String content) {
        // Folosim Platform.runLater pentru a asigura că alertele sunt afișate pe UI thread
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Metodă pentru a reveni la ecranul New Game sau la meniul principal Minesweeper
    private void returnToNewGameScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperNewGameScreen.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) boardGrid.getScene().getWindow(); // Obținem Stage de la orice nod din scenă
                stage.setScene(new Scene(root));
                stage.setTitle("Minesweeper - New Game");
                stage.show();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to return to Minesweeper New Game screen: " + e.getMessage(), e);
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to new game screen.");
            }
        });
    }

    // Aceste metode sunt pentru meniul principal Minesweeper.
    // Daca MinesweeperForm este si pentru meniu SI pentru joc, atunci OK.
    // Daca ai MinesweeperMainMenu.fxml si MinesweeperBoard.fxml atunci trebuie sa separi.
    // Din exemplul tau TicTacToe, TicTacToeForm este meniul, iar TicTacToeBoard este tabla de joc.
    // Deci, ar trebui sa ai MinesweeperMainMenu.java si MinesweeperBoard.java.
    // Acum, ca sa nu facem prea multe, o sa presupun ca MinesweeperForm este controller-ul pentru tabla de joc.
    // Functiile onNewGameClick, onLoadGameClick, onScoreboardClick, onBackClick, onExitClick
    // sunt probabil din meniul principal de Minesweeper (cel care duce la MinesweeperNewGameScreen),
    // nu din tabla de joc.
    // Le voi lăsa deocamdată ca și comentarii sau ca atare, dar ține cont de separare.
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

    // Asigură-te că aceste metode sunt în controlerul potrivit (meniu vs. board)
    @FXML
    private void onLoadGameClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Load Game", "Funcționalitatea 'Load Game' Minesweeper nu este încă implementată.");
        logger.log(Level.INFO, "Load Game Minesweeper clicked.");
        // Logică similară cu TicTacToeForm.onLoadClick, dar pentru Minesweeper
    }

    @FXML
    private void onScoreboardClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/scoreFormMinesweeper.fxml")); // Asigură-te că e corect numele fișierului
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