package org.example.game_library.networking.server.minesweeper_game_logic;

public class MinesweeperGameGenerator {
    public static MinesweeperGameState generateBoard(int rows, int cols, int mines) {
        MinesweeperGameState game = new MinesweeperGameState(rows, cols);
        game.placeMines(mines);
        game.calculateNumbers();
        return game;
    }
}
