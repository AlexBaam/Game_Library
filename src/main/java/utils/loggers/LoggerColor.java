package utils.loggers;

import java.util.logging.*;

public class LoggerColor extends Formatter {

    // Coduri ANSI pentru culori
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    @Override
    public String format(LogRecord record) {
        String color;

        switch (record.getLevel().getName()) {
            case "SEVERE": color = RED; break;
            case "WARNING": color = YELLOW; break;
            case "INFO": color = GREEN; break;
            case "FINE":
            case "FINER":
            case "FINEST": color = BLUE; break;
            default: color = RESET;
        }

        return String.format("%s[%s] %s%s%n",
                color,
                record.getLevel().getName(),
                formatMessage(record),
                RESET);
    }
}