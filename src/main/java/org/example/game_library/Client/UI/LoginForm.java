package org.example.game_library.Client.UI;

import Utils.AppLogger;
import Utils.Exceptions.NullData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginForm {
    private static final Logger logger = AppLogger.getLogger();
    private String username;
    private String password;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private void onLoginClick() {
        this.username = usernameField.getText();
        this.password = passwordField.getText();

        logger.log(Level.INFO, "User pressed login");

        try{
            if(username.isBlank()){
                throw new NullData("Username cannot be blank");
            }

            if(password.isBlank()){
                throw new NullData("Password cannot be blank");
            }

            logger.log(Level.INFO, "Username entered: {0}", username);
            logger.log(Level.INFO, "Password entered: {0}", password);

        } catch (NullData e){
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        }

        // TODO: Implement real login logic (server call, validation, etc.)
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