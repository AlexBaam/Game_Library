package org.example.game_library.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "game_types")
public class GameType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_type_id")
    private int gameTypeId;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    public GameType() {}

    public GameType(String name) {
        this.name = name;
    }
}