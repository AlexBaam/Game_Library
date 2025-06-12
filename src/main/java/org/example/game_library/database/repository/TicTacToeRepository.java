package org.example.game_library.database.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.example.game_library.database.model.TicTacToe;
import org.example.game_library.database.model.User;
import org.example.game_library.networking.server.tictactoe_game_logic.ScoreEntry;
import org.example.game_library.utils.jpa.JPAUtils;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeRepository {

    public static void incrementWins(User user, String mode) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            TicTacToe score = em.find(TicTacToe.class, user.getUserId());

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

    public static List<ScoreEntry> getTicTacToeTopRankedPlayers(int topRanks, String scoreType) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            if (!scoreType.equals("network_wins") && !scoreType.equals("ai_wins")) {
                throw new IllegalArgumentException("Tip de scor invalid furnizat pentru clasament: " + scoreType);
            }

            List<Object[]> resultList = em.createNativeQuery(
                            "SELECT rank_nr, username, wins FROM get_tictactoe_top_ranked_players(?, ?)")
                    .setParameter(1, topRanks)
                    .setParameter(2, scoreType)
                    .getResultList();

            List<ScoreEntry> topPlayers = new ArrayList<>();
            for (Object[] row : resultList) {
                int rank = ((Number) row[0]).intValue();
                String username = (String) row[1];
                int wins = ((Number) row[2]).intValue();
                topPlayers.add(new ScoreEntry(rank, username, wins));
            }
            return topPlayers;
        } catch (Exception e) {
            throw new PersistenceException("Error retrieving Minesweeper scores: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
