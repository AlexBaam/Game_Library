package org.example.game_library.networking.server.minesweeper_game_logic;

import java.io.Serializable; // Adaugă importul
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Asigură-te că și Cell este Serializable!
public class MinesweeperGame implements Serializable {
    private static final long serialVersionUID = 1L; // Recomandat pentru Serializable

    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int unrevealedNonMineCells; // Numărul de celule non-mine încă acoperite
    private int flaggedCellsCount; // Numărul de celule marcate cu steag
    private boolean gameOver;
    private boolean gameWon;
    private boolean firstClickMade;

    // Poți folosi un enum pentru GameState (PLAYING, WON, LOST) dacă vrei o stare mai complexă
    // private GameState currentGameState;

    public MinesweeperGame(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.unrevealedNonMineCells = rows * cols - totalMines;
        this.flaggedCellsCount = 0;
        this.board = new Cell[rows][cols];
        this.gameOver = false;
        this.gameWon = false;
        this.firstClickMade = false;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell(r, c);
            }
        }
    }

    // Metoda pentru plasarea minelor, apelată la primul click
    public void placeMines(int firstClickRow, int firstClickCol) {
        if (firstClickMade) {
            return;
        }
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // Asigură-te că nu punem mina pe prima celulă apăsată sau pe o celulă deja minată
            // Și asigură-te că nu punem mine în jurul primului click (3x3 grid around first click)
            boolean isNearFirstClick = (Math.abs(r - firstClickRow) <= 1 && Math.abs(c - firstClickCol) <= 1);

            if (!board[r][c].isMine() && !isNearFirstClick) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }
        calculateAdjacentMines();
        firstClickMade = true;
    }

    private void calculateAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isMine()) {
                    int count = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;

                            int newRow = r + dr;
                            int newCol = c + dc;

                            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                                if (board[newRow][newCol].isMine()) {
                                    count++;
                                }
                            }
                        }
                    }
                    board[r][c].setAdjacentMinesCount(count);
                }
            }
        }
    }

    // Returnează o listă de celule actualizate care trebuie trimise clientului
    public List<Cell> revealCell(int r, int c) {
        List<Cell> revealedCells = new ArrayList<>();
        if (gameOver || gameWon || board[r][c].isRevealed() || board[r][c].isFlagged()) {
            return revealedCells; // Nu face nimic
        }

        if (!firstClickMade) {
            placeMines(r, c);
        }

        // Metoda recursivă helper
        revealCellRecursive(r, c, revealedCells);

        if (board[r][c].isMine()) {
            gameOver = true;
            revealAllMines(revealedCells); // Adaugă toate minele la lista de celule descoperite
        } else if (unrevealedNonMineCells == 0) {
            gameWon = true;
            revealAllMines(revealedCells); // Și steagurile corecte dacă vrei
        }
        return revealedCells;
    }

    private void revealCellRecursive(int r, int c, List<Cell> revealedCells) {
        if (r < 0 || r >= rows || c < 0 || c >= cols || board[r][c].isRevealed() || board[r][c].isFlagged() || board[r][c].isMine()) {
            return;
        }

        board[r][c].reveal();
        revealedCells.add(board[r][c]);
        unrevealedNonMineCells--;

        if (board[r][c].getAdjacentMinesCount() == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    revealCellRecursive(r + dr, c + dc, revealedCells);
                }
            }
        }
    }

    public List<Cell> toggleFlag(int r, int c) {
        List<Cell> updatedCells = new ArrayList<>();
        if (gameOver || gameWon || board[r][c].isRevealed()) {
            return updatedCells;
        }
        board[r][c].toggleFlag();
        if (board[r][c].isFlagged()) {
            flaggedCellsCount++;
        } else {
            flaggedCellsCount--;
        }
        updatedCells.add(board[r][c]); // Trimitem înapoi doar celula care a fost flagguită/unflagguită
        return updatedCells;
    }

    // Helper pentru a dezvălui toate minele la Game Over
    private void revealAllMines(List<Cell> cellsToUpdate) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine()) {
                    board[r][c].reveal(); // Marcăm mina ca descoperită
                    cellsToUpdate.add(board[r][c]);
                }
            }
        }
    }

    // Getteri
    public Cell getCell(int r, int c) { return board[r][c]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalMines() { return totalMines; } // Adaugă acest getter
    public int getFlaggedCellsCount() { return flaggedCellsCount; } // Adaugă acest getter
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public boolean isFirstClickMade() { return firstClickMade; }

    // Setter pentru game over (util pentru forfeit)
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
