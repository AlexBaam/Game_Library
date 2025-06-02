package org.example.game_library.views;

import javafx.scene.control.Alert;
import org.example.game_library.database.model.User;
import org.example.game_library.database.repository.UserRepository;
import org.example.game_library.networking.ClientMain;
import org.example.game_library.networking.ClientToServerProxy;
import org.example.game_library.utils.jpa.JPAUtils;
import org.example.game_library.utils.loggers.AppLogger;
import org.example.game_library.utils.exceptions.NullData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;

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

        try {
            if (username.isBlank()) {
                throw new NullData("Username cannot be blank");
            }

            if (email.isBlank()) {
                throw new NullData("Email cannot be blank");
            }

            if (password.isBlank()) {
                throw new NullData("Password cannot be blank");
            }

            logger.log(Level.INFO, "Username entered: {0}", username);
            logger.log(Level.INFO, "Password entered: {0}", password);
            logger.log(Level.INFO, "Email entered: {0}", email);

            int dateVerificate = 1;
            if (username.length() < 4) {
                dateVerificate = 0;
                logger.log(Level.WARNING, "User must have at least 4 characters");
            } else if (password.length() < 4) {
                dateVerificate = 0;
                logger.log(Level.WARNING, "Password must have at least 4 characters");
            } else {
                int numberARond=0, numberDot = 0;
                for (int i = 0; i < email.length(); i++) {
                    if(email.charAt(i)=='.')
                        numberDot++;
                    else if(email.charAt(i)=='@')
                        numberARond++;
                }
                if(numberARond!=1 && numberDot<1) {
                    dateVerificate = 0;
                    logger.log(Level.WARNING, "User must have at least one dot and at one @");
                }
            }

            if(dateVerificate==1) {
                List<String> parameters = new ArrayList<>();
                parameters.clear();

                parameters.add("register");
                parameters.add(email);
                parameters.add(username);
                parameters.add(password);

                //String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                logger.log(Level.INFO, "Preparing to send registration data to server.");

                ClientToServerProxy.send(parameters);

                String response = ClientToServerProxy.receive();

                logger.log(Level.INFO, "Received response: {0}", response);

                if ("SUCCESS".equals(response)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Cont created.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", response);
                }
            }

        } catch (NullData e) {
            logger.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred during registration: {0}", e.getMessage());
        } finally {
            // ne asiguram ca inchidem entitatea
            if (JPAUtils.getEntityManager().isOpen()) {
                JPAUtils.getEntityManager().close();
            }
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}