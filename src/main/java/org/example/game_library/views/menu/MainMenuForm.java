package org.example.game_library.views.menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenuForm {
    private static final Logger logger = AppLogger.getLogger();

    @FXML
    private AnchorPane rootPaneMainMenu;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button exitButton;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        logger.log(Level.INFO, "Main Menu initialized!");

        rootPaneMainMenu.setOnMousePressed(this::handleMousePressed);
        rootPaneMainMenu.setOnMouseDragged(this::handleMouseDragged);
    }

    @FXML
    private void onLoginClick() throws IOException {
        logger.log(Level.INFO, "Switched to login form!");
        switchTo("menu/loginForm.fxml");
    }

    @FXML
    private void onRegisterClick() throws IOException {
        logger.log(Level.INFO, "Switched to register form!");
        switchTo("menu/registerForm.fxml");
    }

    @FXML
    private void onExitClick() {
        logger.log(Level.INFO, "Clicked on exit!");
        System.exit(0);
    }

    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) rootPaneMainMenu.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    private void switchTo(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/" + fxml));
        Parent root = loader.load();
        Stage stage = (Stage) rootPaneMainMenu.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setWidth(800);
        stage.setHeight(720);
    }
}