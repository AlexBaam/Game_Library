package org.example.game_library.utils.loggers;

import java.util.logging.*;

public class LoggerColor extends Formatter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    @Override
    public String format(LogRecord record) {
        String loggerColor;

        switch (record.getLevel().getName()) {
            case "SEVERE": loggerColor = RED; break;
            case "WARNING": loggerColor = YELLOW; break;
            case "INFO": loggerColor = GREEN; break;
            case "FINE": loggerColor = BLUE; break;
            default: loggerColor = RESET;
        }

        return String.format("%s[%s] %s%s%n",
                loggerColor,
                record.getLevel().getName(),
                formatMessage(record),
                RESET);
    }
}