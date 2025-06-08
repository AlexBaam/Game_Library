package org.example.game_library.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "minesweeper_score")
public class Minesweeper {
    @Id
    @Column(name = "user_id")
    private int userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_wins", nullable = false)
    private int totalWins;

    @Column(name = "best_score", nullable = false)
    private int bestScore;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    public Minesweeper() {}

    public Minesweeper(User user) {
        this.user = user;
        this.userId = user.getUser_id();
        this.totalWins = 0;
        this.bestScore = 0;
        this.totalScore = 0;
    }

    @Override
    public String toString() {
        return "TicTacToe [userId=" + userId + ", totalWins=" + totalWins +
                ", bestScore=" + bestScore + ", totalScore=" + totalScore + "]";
    }
}
