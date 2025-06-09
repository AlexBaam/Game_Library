package org.example.game_library.networking.server.minesweeper_game_logic;

import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;

    private int row;
    private int col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMinesCount; // IMPORTANT: Schimbat de la 'adjacentMines' la 'adjacentMinesCount' pentru a se potrivi cu MinesweeperBoard

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMinesCount = 0;
    }

    // Gettere
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isMine() { return isMine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMinesCount() { return adjacentMinesCount; } // Renaming getter

    // Settere
    public void setMine(boolean mine) { isMine = mine; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }
    public void setAdjacentMinesCount(int adjacentMinesCount) { this.adjacentMinesCount = adjacentMinesCount; } // Renaming setter

    // Metodele 'reveal' și 'toggleFlag' care erau cerute de MinesweeperBoard
    public void reveal() {
        this.isRevealed = true;
    }

    public void toggleFlag() {
        this.isFlagged = !this.isFlagged;
    }

    @Override
    public String toString() {
        if (isFlagged) return "F";
        if (!isRevealed) return "#";
        if (isMine) return "M";
        return String.valueOf(adjacentMinesCount);
    }
}