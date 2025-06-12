package org.example.game_library.utils.loggers;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

@Getter
@Setter
public class AppLogger {

    private AppLogger() {}

    @Getter
    private static final Logger logger = Logger.getLogger("GameLibraryLogger");

    static {
        try {
            LogManager.getLogManager().reset();
            logger.setLevel(Level.ALL);

            File logDir = new File("logs");
            if(!logDir.exists()){
                logDir.mkdirs();
            }

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new LoggerColor());
            logger.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler("logs/game_library.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

        } catch (IOException e) {
            logger.severe("Failed to initialize logger: " + e.getMessage());
        }
    }

}