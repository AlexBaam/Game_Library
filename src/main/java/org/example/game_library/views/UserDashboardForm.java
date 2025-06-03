package org.example.game_library.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import org.example.game_library.networking.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDashboardForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    private Button deleteButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button exitButton;

    @FXML
    public void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/loginForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Game Library - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Replace with logger if needed
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed exit button!");

        try{
            List<String> parameters = List.of("exit");

            ClientToServerProxy.send(parameters);

            String response = ClientToServerProxy.receive();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Class not found", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void onDeleteAccClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed delete button");
    }

    public void onMinesweeperClick(MouseEvent mouseEvent) {
        logger.log(Level.INFO, "User pressed minesweeper button");
    }

    public void onTicTacToeClick(MouseEvent mouseEvent) {
        logger.log(Level.INFO, "User pressed tictactoe button");
    }
}