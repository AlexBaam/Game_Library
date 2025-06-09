package org.example.game_library.views.minesweeper;

import org.example.game_library.networking.server.minesweeper_game_logic.Cell;

import java.util.Random;

public class MinesweeperBoard {
    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int unrevealedCells; // Count of non-mine cells that are not revealed yet
    private boolean gameOver;
    private boolean gameWon;
    private long startTime; // For scoring/timer (could be managed by client for display)
    private boolean firstClickMade; // To ensure first click is not a mine

    public MinesweeperBoard(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.unrevealedCells = rows * cols - totalMines; // Initially, all non-mine cells are unrevealed
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

    // Aceasta metoda va fi apelata dupa primul click al jucatorului
    public void placeMines(int firstClickRow, int firstClickCol) {
        if (firstClickMade) {
            return; // Minele au fost deja plasate
        }
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // Asigură-te că nu punem mina pe prima celulă apăsată sau pe o celulă deja minată
            if (!board[r][c].isMine() && !(r == firstClickRow && c == firstClickCol)) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }
        calculateAdjacentMines();
        firstClickMade = true;
        startTime = System.currentTimeMillis(); // Start timer here, if game logic handles it
    }

    private void calculateAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isMine()) {
                    int count = 0;
                    // Verifică celulele vecine (inclusiv diagonale)
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue; // Sare peste celula curentă

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

    // Metoda pentru a gestiona click-ul stânga pe o celulă
    public void revealCell(int r, int c) {
        if (gameOver || gameWon || board[r][c].isRevealed() || board[r][c].isFlagged()) {
            return; // Nu face nimic dacă jocul e gata, celula e deja descoperită sau marcată
        }

        // Dacă e primul click, plasează minele
        if (!firstClickMade) {
            placeMines(r, c);
        }

        board[r][c].reveal();
        if (board[r][c].isMine()) {
            gameOver = true;
            System.out.println("Game Over! You hit a mine."); // For debugging
            // Aici poți adăuga logica pentru afișarea tuturor minelor
        } else {
            unrevealedCells--; // O celulă non-mină a fost descoperită
            if (board[r][c].getAdjacentMinesCount() == 0) {
                // Descoperire în lanț pentru celulele goale
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;

                        int newRow = r + dr;
                        int newCol = c + dc;

                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            if (!board[newRow][newCol].isRevealed() && !board[newRow][newCol].isFlagged()) {
                                revealCell(newRow, newCol); // Recursivitate
                            }
                        }
                    }
                }
            }
            if (unrevealedCells == 0) {
                gameWon = true;
                System.out.println("Congratulations! You won!"); // For debugging
                // Aici poți adăuga logica de calcul scor și salvare
            }
        }
    }

    // Metoda pentru a gestiona click-ul dreapta (marcare/demarcare steag)
    public void toggleFlag(int r, int c) {
        if (gameOver || gameWon || board[r][c].isRevealed()) {
            return;
        }
        board[r][c].toggleFlag();
        // Poate ai nevoie să actualizezi un contor de steaguri în UI
    }

    // Getteri necesari pentru Controller
    public Cell getCell(int r, int c) { return board[r][c]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public boolean isFirstClickMade() { return firstClickMade; }

    // Poți adăuga metode pentru a obține starea completă a tablei (pentru salvare)
    // public Cell[][] getBoardState() { return board; }
}
