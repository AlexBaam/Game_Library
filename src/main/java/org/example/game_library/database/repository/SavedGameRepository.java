package org.example.game_library.database.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.game_library.database.model.GameType;
import org.example.game_library.database.model.SavedGame;
import org.example.game_library.database.model.User;
import org.example.game_library.networking.server.tictactoe_game_logic.TicTacToeGame;
import org.example.game_library.utils.jpa.JPAUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SavedGameRepository {

    private static final ObjectMapper mapper = new ObjectMapper();

    private SavedGameRepository() {}

    public static void saveGame(User user, String gameTypeName, TicTacToeGame game) throws Exception {
        EntityManager em = JPAUtils.getEntityManager();

        try {
            GameType gameType = em.createQuery(
                            "SELECT g FROM GameType g WHERE LOWER(g.name) = :name", GameType.class)
                    .setParameter("name", gameTypeName.toLowerCase())
                    .getSingleResult();

            String gameStateJson = mapper.writeValueAsString(game);

            em.getTransaction().begin();

            em.createNativeQuery("""
                INSERT INTO saved_games (user_id, game_type_id, game_state, saved_at)
                VALUES (?1, ?2, CAST(?3 AS jsonb), ?4)
            """)
                    .setParameter(1, user.getUser_id())
                    .setParameter(2, gameType.getGameTypeId())
                    .setParameter(3, gameStateJson)
                    .setParameter(4, LocalDateTime.now())
                    .executeUpdate();

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static List<TicTacToeGame> loadGamesForUser(User user, String gameTypeName) throws Exception {
        EntityManager em = JPAUtils.getEntityManager();

        try {
            TypedQuery<SavedGame> query = em.createQuery("""
                SELECT s FROM SavedGame s
                WHERE s.user = :user AND LOWER(s.gameType.name) = :name
                ORDER BY s.savedAt DESC
            """, SavedGame.class);

            query.setParameter("user", user);
            query.setParameter("name", gameTypeName.toLowerCase());

            List<SavedGame> savedGames = query.getResultList();
            List<TicTacToeGame> games = new ArrayList<>();

            for (SavedGame savedGame : savedGames) {
                TicTacToeGame game = mapper.readValue(savedGame.getGameStateJSON(), TicTacToeGame.class);
                games.add(game);
            }

            return games;
        } finally {
            em.close();
        }
    }
}