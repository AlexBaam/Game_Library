package org.example.game_library.networking.server.minesweeper_game_logic;

import java.io.Serializable;

public class ScoreEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private int rank;
    private String username;
    private int totalScore;

    public ScoreEntry(int rank, String username, int totalScore) {
        this.rank = rank;
        this.username = username;
        this.totalScore = totalScore;
    }

    public int getRank() { return rank; }
    public String getUsername() { return username; }
    public int getTotalScore() { return totalScore; }

    public void setRank(int rank) { this.rank = rank; }
    public void setUsername(String username) { this.username = username; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
}