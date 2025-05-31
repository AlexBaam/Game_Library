package org.example.game_library.views;

import utils.loggers.AppLogger;
import utils.exceptions.NullData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterForm {
    private String username;
    private String password;
    private String email;
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private void onRegisterClick() {
        this.username = usernameField.getText();
        this.password = passwordField.getText();
        this.email = emailField.getText();

        logger.log(Level.INFO, "User pressed register");

        try{
            if(username.isBlank()){
                throw new NullData("Username cannot be blank");
            }

            if(email.isBlank()){
                throw new NullData("Email cannot be blank");
            }

            if(password.isBlank()){
                throw new NullData("Password cannot be blank");
            }

            logger.log(Level.INFO, "Username entered: {0}", username);
            logger.log(Level.INFO, "Password entered: {0}", password);
            logger.log(Level.INFO, "Email entered: {0}", email);
        } catch (NullData e) {
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        }

        //TODO ACTUAL SERVER COMMUNICATION
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/mainMenuForm.fxml"));
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