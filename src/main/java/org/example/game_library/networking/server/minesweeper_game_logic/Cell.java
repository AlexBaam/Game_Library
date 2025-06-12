package org.example.game_library.networking.server.minesweeper_game_logic;

import java.io.Serializable;

public class Cell implements Serializable {
    private boolean hasMine;
    private int adjacentMines;
    private boolean revealed;
    private boolean flagged;

    public Cell() {
        this.hasMine = false;
        this.adjacentMines = 0;
        this.revealed = false;
    }

    public boolean hasMine() {
        return hasMine;
    }

    public void setHasMine(boolean hasMine) {
        this.hasMine = hasMine;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}


