package org.example.game_library.Client.UI;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterForm {
    private String username;
    private String password;
    private String email;

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

        System.out.println("Username: " + usernameField.getText());
        System.out.println("Email: " + emailField.getText());
        System.out.println("Password: " + passwordField.getText());
    }

    @FXML
    private void onBackClick() {
        System.out.println("Back button clicked");
        // TODO: Load mainMenu.fxml if needed
    }

    @FXML
    private void onExitClick() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}