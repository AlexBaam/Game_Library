package org.example.game_library.networking.enums;

public enum CommandMinesweeper {

    NEWGAME("newgame"),
    LOADGAME("load"),
    SCORE("score"),
    EXIT("exit"),
    SAVEGAME("save"),
    REVEAL("reveal"),
    FLAG("flag"),
    FORFEIT("forfeit");

    private final String commandMinesweeper;

    CommandMinesweeper(String command) {
        this.commandMinesweeper = command;
    }

    public static CommandMinesweeper fromString(String value) {
        for(CommandMinesweeper c : CommandMinesweeper.values()) {
            if(c.commandMinesweeper.equalsIgnoreCase(value)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.commandMinesweeper;
    }
}

