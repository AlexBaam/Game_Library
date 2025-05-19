package org.example.game_library.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMain extends Application {

    public static Socket socket;
    public static DataOutputStream out;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Attempt to connect to the server
            socket = new Socket("127.0.0.1", 5000);
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("✅ Connected to server.");
        } catch (IOException e) {
            System.out.println("❌ Failed to connect to server: " + e.getMessage());
            return; // Stop if connection failed
        }

        // Load and display the main menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/game_library/FXML/mainMenuForm.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);

        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}