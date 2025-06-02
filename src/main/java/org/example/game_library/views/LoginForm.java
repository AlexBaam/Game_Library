package org.example.game_library.views;

import jakarta.persistence.EntityManager;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.utils.jpa.JPAUtils;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.exceptions.NullData;
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

            logger.log(Level.INFO, "Attempting login for user: {0}", username);

            EntityManager em = JPAUtils.getEntityManager();
            UserRepository userRepo = new UserRepository(em);
            User user = userRepo.authenticate(username, password);

            if(user != null){
                FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/org/example/game_library/FXML/userDashboardForm.fxml"));
                Parent root = fxmlLoader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                logger.log(Level.INFO, "Login successful! Switched to dashboard!");
            } else {
                logger.log(Level.WARNING, "Login failed for user: {0}", username);
            }

        } catch (NullData e){
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading user dashboard: {0}", e.getMessage());
            throw new RuntimeException(e);
        }
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