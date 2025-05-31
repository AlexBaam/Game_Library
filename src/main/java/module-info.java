module org.example.game_library {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires java.logging;

    exports org.example.game_library.Client;
    opens org.example.game_library.Client to javafx.fxml;

    exports org.example.game_library.Client.UI;
    opens org.example.game_library.Client.UI to javafx.fxml;

    exports org.example.game_library.Server;
    opens org.example.game_library.Server to javafx.fxml;
}
