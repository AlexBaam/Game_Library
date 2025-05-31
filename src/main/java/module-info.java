module org.example.game_library {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires java.logging;

    exports org.example.game_library.networking;
    opens org.example.game_library.networking to javafx.fxml;

    exports org.example.game_library.views;
    opens org.example.game_library.views to javafx.fxml;
}
