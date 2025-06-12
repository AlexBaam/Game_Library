package org.example.game_library.views.menu;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.exceptions.NullData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.game_library.utils.ui.ShowAlert;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    public AnchorPane rootPaneRegister;

    @FXML
    public Button registerButton;

    @FXML
    public Button backButton;

    @FXML
    public Button exitButton;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private void onRegisterClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        logger.log(Level.INFO, "User pressed register");

        try {
            validateNotBlank(username, "Username");
            validateNotBlank(email, "Email");
            validateNotBlank(password, "Password");

            logger.log(Level.INFO,
                    "Username entered: {0} \n Password entered: {1} \n Email entered: {2}",
                    new Object[]{username, password, email}
            );

            if (!isValidInput(username, password, email)) {
                return;
            }

            List<String> parameters = List.of("register", email, username, password);

            logger.log(Level.INFO, "Preparing to send registration data to server.");

            ClientToServerProxy.send(parameters);
            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received response: {0}", response);

            if ("SUCCESS".equals(response)) {
                logger.log(Level.INFO, "Account successfully registered into the database!");
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created!");

                FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/org/example/game_library/FXML/menu/userDashboardForm.fxml"));
                Parent root = fxmlLoader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                logger.log(Level.INFO, "Redirected to user dashboard after successful registration.");
            } else {
                logger.log(Level.WARNING, "Registration failed! Reason: {0}", response);
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Registration Failed", response);
            }
        } catch (NullData e) {
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred during registration: {0}", e.getMessage());
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

    private void validateNotBlank(String field, String fieldName) throws NullData {
        logger.log(Level.INFO, "Validating field: {0}", fieldName);
        if (field == null || field.isBlank()) {
            throw new NullData(fieldName + " cannot be blank");
        }
    }

    private boolean isValidInput(String username, String password, String email) {
        if (username.length() < 4) {
            logger.log(Level.WARNING, "User must have at least 4 characters");
            return false;
        }

        if (password.length() < 4) {
            logger.log(Level.WARNING, "Password must have at least 4 characters");
            return false;
        }

        int atCount = 0;
        int dotCount = 0;
        for (char ch : email.toCharArray()) {
            if (ch == '@'){
                atCount++;
            }
            if (ch == '.'){
                dotCount++;
            }
        }

        if (atCount != 1 || dotCount < 1) {
            logger.log(Level.WARNING, "User must have at least one dot and one @");
            return false;
        }

        return true;
    }
}