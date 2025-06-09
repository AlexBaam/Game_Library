package org.example.game_library.networking.server.minesweeper_game_logic;

import org.example.game_library.networking.enums.DifficultyLevel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinesweeperGame implements Serializable {
    private static final long serialVersionUID = 1L; // Adaugă asta pentru serializare
    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int revealedCellsCount;
    private int flaggedCellsCount;
    private boolean gameOver;
    private boolean gameWon;
    private Random random;

    // Asigură-te că ai constructorul care primește DifficultyLevel
    public MinesweeperGame(DifficultyLevel difficulty) {
        this.random = new Random(); // Inițializează Random

        switch (difficulty) {
            case EASY:
                this.rows = 9;
                this.cols = 9;
                this.totalMines = 10;
                break;
            case MEDIUM:
                this.rows = 16;
                this.cols = 16;
                this.totalMines = 40;
                break;
            case HARD:
                this.rows = 16;
                this.cols = 30; // Dimensiuni standard pentru Hard
                this.totalMines = 99; // Mine standard pentru Hard
                break;
            default:
                // Fallback, deși ideal ar trebui să fie acoperite toate cazurile
                this.rows = 9;
                this.cols = 9;
                this.totalMines = 10;
        }
        initializeBoard(); // Apelul acestei metode este crucial
    }

    // Sau, dacă aveai un constructor cu int-uri și vrei să îl păstrezi:
    // public MinesweeperGame(int rows, int cols, int totalMines) {
    //     this.rows = rows;
    //     this.cols = cols;
    //     this.totalMines = totalMines;
    //     this.random = new Random();
    //     initializeBoard();
    // }


    private void initializeBoard() {
        this.board = new Cell[rows][cols];
        this.revealedCellsCount = 0;
        this.flaggedCellsCount = 0;
        this.gameOver = false;
        this.gameWon = false;

        // Inițializează toate celulele
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell(r, c);
            }
        }

        // Plasează minele
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!board[r][c].isMine()) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }

        // Calculează numărul de mine adiacente pentru fiecare celulă
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isMine()) {
                    int mineCount = countAdjacentMines(r, c);
                    board[r][c].setAdjacentMinesCount(mineCount);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        // Verifică celulele în jurul celulei curente (3x3)
        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {
                if (rOffset == 0 && cOffset == 0) continue; // Sari peste celula curentă

                int newRow = row + rOffset;
                int newCol = col + cOffset;

                // Verifică dacă coordonatele sunt valide și dacă celula conține o mină
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && board[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Metodele revealCell, toggleFlag, isGameOver, isGameWon, getRows, getCols, getTotalMines, getFlaggedCellsCount
    // trebuie să existe și să fie corecte. Voi include doar signaturile lor pentru a nu complica.

    // Această metodă ar trebui să returneze o listă de celule actualizate
    public List<Cell> revealCell(int row, int col) {
        List<Cell> revealed = new ArrayList<>();
        // Implementează logica de dezvăluire recursivă dacă celula e goală (0 mine adiacente)
        // Marchează celula ca revealed, incrementează revealedCellsCount
        // Dacă e mină, gameOver = true
        // Verifică condiția de câștig (revealedCellsCount == (rows * cols - totalMines))
        return revealed; // Asigură-te că returnează o listă validă
    }

    public List<Cell> toggleFlag(int row, int col) {
        List<Cell> updated = new ArrayList<>();
        // Implementează logica de toggle flag
        // Actualizează flaggedCellsCount
        return updated; // Asigură-te că returnează o listă validă
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public int getFlaggedCellsCount() {
        return flaggedCellsCount;
    }

    // Metoda pentru a obține o celulă anume, utilă pentru logica internă
    public Cell getCell(int r, int c) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            return board[r][c];
        }
        return null;
    }

    // Clasa Cell trebuie să fie și ea Serializable
    // Asigură-te că celulele returnate către client conțin doar informațiile necesare
    // (rând, coloană, isRevealed, isFlagged, adjacentMines (dacă isRevealed), isMine (doar la sfârșitul jocului))
}