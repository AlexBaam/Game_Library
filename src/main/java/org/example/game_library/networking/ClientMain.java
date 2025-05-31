package org.example.game_library.networking;

import org.example.game_library.utils.loggers.AppLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain extends Application {

    public static Socket socket;
    public static DataOutputStream out;
    private static final Logger logger = AppLogger.getLogger();

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Attempt to connect to the server
            socket = new Socket("127.0.0.1", 5000);
            out = new DataOutputStream(socket.getOutputStream());
            logger.log(Level.INFO, "Client connected");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client connection error: {0}", e.getMessage());
            return; // stop if connection failed
        }

        // Load and display the main menu
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/mainMenuForm.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            scene.setFill(Color.TRANSPARENT);

            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Interface loading error: {0}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}