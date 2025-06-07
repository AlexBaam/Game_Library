package org.example.game_library.database.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.game_library.database.model.TicTacToe;
import org.example.game_library.database.model.User;
import org.example.game_library.utils.jpa.JPAUtils;

public class TicTacToeRepository {

    public static void incrementWins(User user, String mode) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            TicTacToe score = em.find(TicTacToe.class, user.getUser_id());

            if (score == null) {
                score = new TicTacToe(user);
            }

            switch (mode.toLowerCase()) {
                case "local" -> score.setLocalWins(score.getLocalWins() + 1);
                case "network" -> score.setNetworkWins(score.getNetworkWins() + 1);
                case "ai" -> score.setAiWins(score.getAiWins() + 1);
                default -> throw new IllegalArgumentException("Mod invalid: " + mode);
            }

            em.merge(score);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
