package org.example.game_library.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
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
    public void handleLogout(ActionEvent event) {
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
    public void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void handleDeleteAcc(ActionEvent actionEvent) {
        logger.log(Level.INFO, "User pressed delete button");
    }
}