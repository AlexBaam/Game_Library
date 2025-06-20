package org.example.game_library.views.menu;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.exceptions.NullData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.game_library.utils.ui.ShowAlert;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoginForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public Button loginButton;

    @FXML
    public Button backButton;

    @FXML
    public Button exitButton;

    @FXML
    public AnchorPane rootPaneLogin;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.log(Level.INFO, "User pressed login");

        try{
            if(username.isBlank()){
                throw new NullData("Username cannot be blank");
            }

            if(password.isBlank()){
                throw new NullData("Password cannot be blank");
            }

            logger.log(Level.INFO, "Attempting login for user: {0}", username);

            List<String> parameters = List.of( "login", username, password);

            logger.log(Level.INFO, "Preparing to send login data to server.");

            ClientToServerProxy.send(parameters);

            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received response: {0}", response);

            if ("SUCCESS".equals(response)) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Login successful", "User successfully logged in.");
                FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/org/example/game_library/FXML/menu/userDashboardForm.fxml"));
                Parent root = fxmlLoader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                logger.log(Level.INFO, "Login successful! Switched to dashboard!");
            } else {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Login Failed", response);
                logger.log(Level.WARNING, "Login failed for user: {0}. Response: {1}", new Object[]{username, response});
            }

        } catch (NullData e){
            ShowAlert.showAlert(Alert.AlertType.WARNING, "Validation Error", e.getMessage());
            logger.log(Level.WARNING, "Validation error: {0}", e.getMessage());
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Connection Error", e.getMessage());
            logger.log(Level.SEVERE, "Error loading user dashboard or connection error: {0}", e.getMessage());
        } catch (ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Communication Error", e.getMessage());
            logger.log(Level.SEVERE, "Error receiving data from server: {0}", e.getMessage());
        }
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/mainMenuForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @FXML
    private void onExitClick() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}