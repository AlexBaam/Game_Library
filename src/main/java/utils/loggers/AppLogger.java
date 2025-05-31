package utils.loggers;
import java.io.IOException;
import java.util.logging.*;

public class AppLogger {

    private static final Logger logger = Logger.getLogger("GameLibraryLogger");

    static {
        try {
            LogManager.getLogManager().reset();
            logger.setLevel(Level.ALL);

            // Use colored console output
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new LoggerColor()); // Aici setez ca loggerul meu sa fie setat dupa propriile reguli
            logger.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler("game_library.log", true);
            fileHandler.setLevel(Level.FINE);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

        } catch (IOException e) {
            logger.severe("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}