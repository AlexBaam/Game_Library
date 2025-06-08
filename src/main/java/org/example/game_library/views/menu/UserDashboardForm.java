package org.example.game_library.views.menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import org.example.game_library.utils.loggers.AppLogger;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Alert;

import org.example.game_library.networking.client.ClientToServerProxy;
import org.example.game_library.utils.ui.ShowAlert;

import java.util.List;

public class UserDashboardForm {
    private static final Logger logger = AppLogger.getLogger();

    private static final String COMM_ERR_HEADER = "Communication Error";
    private static final String COMM_ERR_BODY = "Error writing to the server!";

    private static final String PROT_ERR_HEADER = "Protocol Error";
    private static final String PROT_ERR_BODY = "Error reading from the server!";

    private static final String SUCCES = "SUCCESS";

    @FXML
    public ImageView minesweeperButton;

    @FXML
    public ImageView tictactoeButton;

    @FXML
    public Button deleteAccButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button exitButton;

    @FXML
    public void onLogoutClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed logout button.");
        try {
            List<String> parameters = List.of("logout");
            ClientToServerProxy.send(parameters);

            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received logout response from server: {0}", response);

            if (SUCCES.equals(response)) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Logout", "Logout successful! Navigating to login form.");
                logger.log(Level.INFO, "Logout successful! Navigating to login form.");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/loginForm.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Game Library - Login");
                stage.show();
            } else {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Logout", response);
                logger.log(Level.WARNING, "Logout failed. Server response: {0}", response);
            }
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, COMM_ERR_HEADER, COMM_ERR_BODY);
            logger.log(Level.SEVERE, "IO Exception during logout: {0}", e.getMessage());
        } catch (ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, PROT_ERR_HEADER, PROT_ERR_BODY);
            logger.log(Level.SEVERE, "ClassNotFound Exception during logout: {0}", e.getMessage());
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Unknown Error", "An error occurred while logging out!");
            logger.log(Level.SEVERE, "Unexpected Exception during logout: {0}", e.getMessage());
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        logger.log(Level.INFO, "User pressed exit button.");
        try {
            ClientToServerProxy.send(List.of("exit"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not send exit command to server: {0}", e.getMessage());
        } finally {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    public void onDeleteAccClick(ActionEvent actionEvent) {
        logger.log(Level.INFO, "User pressed delete account button");
        try {
            List<String> parameters = List.of("delete");
            ClientToServerProxy.send(parameters);

            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received delete account response from server: {0}", response);

            if (SUCCES.equals(response)) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Delete", "Account deleted successfully!");
                logger.log(Level.INFO, "Account deletion successful! Navigating to register form.");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/menu/registerForm.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Game Library - Login");
                stage.show();
            } else {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Delete", response);
                logger.log(Level.WARNING, "Account deletion failed. Server response: {0}", response);
            }
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, COMM_ERR_HEADER, COMM_ERR_BODY);
            logger.log(Level.SEVERE, "IO Exception during account deletion: {0}", e.getMessage());
        } catch (ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, PROT_ERR_HEADER, PROT_ERR_BODY);
            logger.log(Level.SEVERE, "ClassNotFoundException during account deletion: {0}", e.getMessage());
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Unknown Error", "Unknown error occurred while deleting account!");
            logger.log(Level.SEVERE, "Unexpected error during account deletion: {0}", e.getMessage());
        }
    }

    public void onMinesweeperClick(MouseEvent mouseEvent) {
        logger.log(Level.INFO, "User pressed Minesweeper button!");
        try{
            List<String> parameters = List.of("minesweeper");
            ClientToServerProxy.send(parameters);

            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received minesweeper response from server: {0}", response);

            if(SUCCES.equals(response)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/minesweeper/minesweeperForm.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                logger.log(Level.WARNING, "Minesweeper loading failed! Reason: {0}", response);
            }
        } catch (ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, PROT_ERR_HEADER, PROT_ERR_BODY);
            logger.log(Level.SEVERE, "ClassNotFoundException during minesweeper loading: {0}", e.getMessage());
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, COMM_ERR_HEADER, COMM_ERR_BODY);
            logger.log(Level.SEVERE, "IO Exception during minesweeper loading: {0}", e.getMessage());
        }
    }

    public void onTicTacToeClick(MouseEvent mouseEvent) {
        logger.log(Level.INFO, "User pressed TicTacToe button!");
        try{
            List<String> parameters = List.of("tictactoe");
            ClientToServerProxy.send(parameters);

            String response = (String) ClientToServerProxy.receive();

            logger.log(Level.INFO, "Received delete account response from server: {0}", response);

            if(SUCCES.equals(response)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/tictactoe/tictactoeForm.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                logger.log(Level.WARNING, "TicTacToe loading failed! Reason: {0}", response);
            }
        } catch (ClassNotFoundException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, PROT_ERR_HEADER, PROT_ERR_BODY);
            logger.log(Level.SEVERE, "ClassNotFoundException during tictactoe loading: {0}", e.getMessage());
        } catch (IOException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, COMM_ERR_HEADER, COMM_ERR_BODY);
            logger.log(Level.SEVERE, "IO Exception during tictactoe loading: {0}", e.getMessage());
        }
    }
}

