package org.example.game_library.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "tictactoe_scores")
public class TicTacToe {

    @Id
    @Column(name = "user_id")
    private int userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "local_wins", nullable = false)
    private int localWins;

    @Column(name = "network_wins", nullable = false)
    private int networkWins;

    @Column(name = "ai_wins", nullable = false)
    private int aiWins;

    public TicTacToe() {}

    public TicTacToe(User user) {
        this.user = user;
        this.userId = user.getUser_id();
        this.localWins = 0;
        this.networkWins = 0;
        this.aiWins = 0;
    }

    @Override
    public String toString() {
        return "TicTacToe [userId=" + userId + ", localWins=" + localWins +
                ", networkWins=" + networkWins + ", aiWins=" + aiWins + "]";
    }
}