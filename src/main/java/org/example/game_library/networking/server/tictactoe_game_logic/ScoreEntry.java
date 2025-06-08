package org.example.game_library.networking.server.tictactoe_game_logic;

import java.io.Serializable;

public class ScoreEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private int rank;
    private String username;
    private int wins;

    public ScoreEntry(int rank, String username, int wins) {
        this.rank = rank;
        this.username = username;
        this.wins = wins;
    }

    public int getRank() { return rank; }
    public String getUsername() { return username; }
    public int getWins() { return wins; }

    public void setRank(int rank) { this.rank = rank; }
    public void setUsername(String username) { this.username = username; }
    public void setWins(int wins) { this.wins = wins; }
}