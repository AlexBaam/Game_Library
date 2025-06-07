package org.example.game_library.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "saved_games")
public class SavedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "save_id")
    private int saveId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_type_id", nullable = false)
    private GameType gameType;

    @Column(name = "game_state", columnDefinition = "jsonb", nullable = false)
    private String gameStateJSON;

    @Column(name = "saved_at", columnDefinition = "timestamp", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public SavedGame() {}

    public SavedGame(User user, GameType gameType, String gameStateJSON) {
        this.user = user;
        this.gameType = gameType;
        this.gameStateJSON = gameStateJSON;
        this.savedAt = LocalDateTime.now();
    }
}