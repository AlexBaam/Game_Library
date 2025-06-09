package org.example.game_library.networking.server.minesweeper_game_logic;

import org.example.game_library.networking.enums.DifficultyLevel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinesweeperGame implements Serializable {
    private static final long serialVersionUID = 1L;
    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int revealedCellsCount;
    private int flaggedCellsCount;
    private boolean gameOver;
    private boolean gameWon;
    private Random random;
    private boolean firstClickMade;

    public MinesweeperGame(DifficultyLevel difficulty) {
        this.random = new Random();
        this.firstClickMade = false;

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
                this.cols = 30;
                this.totalMines = 99;
                break;
            default:
                this.rows = 9;
                this.cols = 9;
                this.totalMines = 10;
        }
        initializeBoard();
    }

    private void initializeBoard() {
        this.board = new Cell[rows][cols];
        this.revealedCellsCount = 0;
        this.flaggedCellsCount = 0;
        this.gameOver = false;
        this.gameWon = false;

        // Initialize all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell(r, c);
            }
        }
    }

    private void placeMines(int firstClickRow, int firstClickCol) {
        if (firstClickMade) {
            return;
        }

        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // Don't place mine on first click or on already mined cell
            if (!board[r][c].isMine() && !(r == firstClickRow && c == firstClickCol)) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }

        // Calculate adjacent mines for all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isMine()) {
                    int mineCount = countAdjacentMines(r, c);
                    board[r][c].setAdjacentMinesCount(mineCount);
                }
            }
        }

        firstClickMade = true;
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {
                if (rOffset == 0 && cOffset == 0) continue;

                int newRow = row + rOffset;
                int newCol = col + cOffset;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && board[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<Cell> revealCell(int row, int col) {
        List<Cell> revealedCells = new ArrayList<>();

        // Validate coordinates
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return revealedCells;
        }

        Cell cell = board[row][col];

        // Can't reveal if game is over, cell is already revealed, or cell is flagged
        if (gameOver || gameWon || cell.isRevealed() || cell.isFlagged()) {
            return revealedCells;
        }

        // Place mines on first click
        if (!firstClickMade) {
            placeMines(row, col);
        }

        // Reveal the cell
        cell.setRevealed(true);
        revealedCells.add(cell);
        revealedCellsCount++;

        if (cell.isMine()) {
            // Game over - reveal all mines
            gameOver = true;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (board[r][c].isMine() && !board[r][c].isRevealed()) {
                        board[r][c].setRevealed(true);
                        revealedCells.add(board[r][c]);
                    }
                }
            }
        } else {
            // If cell has no adjacent mines, reveal adjacent cells recursively
            if (cell.getAdjacentMinesCount() == 0) {
                for (int rOffset = -1; rOffset <= 1; rOffset++) {
                    for (int cOffset = -1; cOffset <= 1; cOffset++) {
                        if (rOffset == 0 && cOffset == 0) continue;

                        int newRow = row + rOffset;
                        int newCol = col + cOffset;

                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            Cell adjacentCell = board[newRow][newCol];
                            if (!adjacentCell.isRevealed() && !adjacentCell.isFlagged()) {
                                List<Cell> additionalRevealed = revealCell(newRow, newCol);
                                revealedCells.addAll(additionalRevealed);
                            }
                        }
                    }
                }
            }

            // Check win condition
            int totalNonMineCells = rows * cols - totalMines;
            if (revealedCellsCount >= totalNonMineCells) {
                gameWon = true;
            }
        }

        return revealedCells;
    }

    public List<Cell> toggleFlag(int row, int col) {
        List<Cell> updatedCells = new ArrayList<>();

        // Validate coordinates
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return updatedCells;
        }

        Cell cell = board[row][col];

        // Can't flag if game is over or cell is already revealed
        if (gameOver || gameWon || cell.isRevealed()) {
            return updatedCells;
        }

        // Toggle flag
        boolean wasFlag = cell.isFlagged();
        cell.setFlagged(!wasFlag);

        // Update flag count
        if (wasFlag) {
            flaggedCellsCount--;
        } else {
            flaggedCellsCount++;
        }

        updatedCells.add(cell);
        return updatedCells;
    }

    // Getters
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

    public Cell getCell(int r, int c) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            return board[r][c];
        }
        return null;
    }
}