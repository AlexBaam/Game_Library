package org.example.game_library.utils.ui;

import javafx.scene.control.Alert;

public class ShowAlert {

    private ShowAlert() {}

    public static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
