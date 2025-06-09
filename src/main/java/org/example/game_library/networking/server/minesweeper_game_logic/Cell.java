package org.example.game_library.networking.server.minesweeper_game_logic;

import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int row;
    private final int col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMinesCount; // 0-8

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMinesCount = 0;
    }

    // Getteri și Setteri
    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void reveal() { isRevealed = true; } // Metoda specială pentru a descoperi

    public boolean isFlagged() { return isFlagged; }
    public void toggleFlag() { isFlagged = !isFlagged; } // Comută steguletul

    public int getAdjacentMinesCount() { return adjacentMinesCount; }
    public void setAdjacentMinesCount(int adjacentMinesCount) {
        this.adjacentMinesCount = adjacentMinesCount;
    }

    // Poate alte metode utile, cum ar fi:
    // public void reset() { /* reseteaza starea celulei pentru un nou joc */ }
}
