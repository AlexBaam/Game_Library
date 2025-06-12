package org.example.game_library.networking.server.minesweeper_game_logic;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

import java.io.Serializable;
import java.util.Random;

public class MinesweeperGameState implements Serializable {
    private int rows, cols;
    private int mineCount;
    private Cell[][] board;

    @FXML
    private GridPane boardGrid;


    public MinesweeperGameState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                board[i][j] = new Cell();
    }

    public void placeMines(int mineCount) {
        this.mineCount = mineCount; // Salvează numărul de mine
        Random random = new Random();
        int placedMines = 0;

        while (placedMines < mineCount) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            if (!board[row][col].hasMine()) {
                board[row][col].setHasMine(true);
                placedMines++;
            }
        }
    }

    public void calculateNumbers() {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].hasMine()) continue;

                int mineCount = 0;
                for (int d = 0; d < 8; d++) {
                    int ni = i + dx[d];
                    int nj = j + dy[d];

                    if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && board[ni][nj].hasMine()) {
                        mineCount++;
                    }
                }
                board[i][j].setAdjacentMines(mineCount);
            }
        }
    }

    public boolean revealCell(int row, int col) {
        // Ignoram daca e in afara tablei sau deja dezvaluita sau are steag
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return false;

        Cell cell = board[row][col];
        if (cell.isRevealed() || cell.isFlagged())
            return false;

        cell.setRevealed(true);

        if (cell.hasMine()) {
            return true; // Ai calcat pe mina!
        }

        // Daca n-are mine in jur, dezvaluim in cascada
        if (cell.getAdjacentMines() == 0) {
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

            for (int d = 0; d < 8; d++) {
                int ni = row + dx[d];
                int nj = col + dy[d];
                revealCell(ni, nj);
            }
        }

        return false;
    }

    public void toggleFlag(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return;

        Cell cell = board[row][col];

        // Nu poti pune steag pe o celula deja dezvaluita
        if (!cell.isRevealed()) {
            cell.setFlagged(!cell.isFlagged());
        }
    }

    public Object getBoardForClient() {
        return board.toString();
    }

    public String flagCell(int x, int y) {
        // Verifică dacă celula este validă
        if (isValidCell(x, y)) {
            Cell cell = board[x][y];  // Extrage celula din tablă

            if (cell.isFlagged()) {
                return "Cell already flagged at " + x + ", " + y;
            }

            cell.setFlagged(true);  // Marchează celula cu steag
            return "Cell flagged at " + x + ", " + y;
        } else {
            return "Invalid cell!";
        }
    }


    public boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Cell getCell(int row, int col) {
        if (isValidCell(row, col)) {
            return board[row][col];
        }
        return null;
    }

    public Cell[][] getBoard() {
        return board; // Returnează referința direct. Este OK atâta timp cât Cell este imutabilă sau clientul nu o modifică.
        // Alternativ, returnează o copie profundă dacă vrei să previi orice modificare.
        // Pentru acest joc, returnarea directă e acceptabilă.
    }


    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMineCount() {
        return mineCount;
    }

    // getter-e si metode auxiliare
}
