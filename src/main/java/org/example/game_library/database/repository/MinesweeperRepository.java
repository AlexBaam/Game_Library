package org.example.game_library.database.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.example.game_library.networking.server.minesweeper_game_logic.ScoreEntryM;
import org.example.game_library.utils.jpa.JPAUtils;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperRepository {

    private MinesweeperRepository() {}

    public static List<ScoreEntryM> getMinesweeperTopRankedPlayers(int topRanks) throws PersistenceException {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            List<Object[]> resultList = em.createNativeQuery(
                            "SELECT rank_nr, username, total_score FROM get_minesweeper_top_ranked_players(?)")
                    .setParameter(1, topRanks)
                    .getResultList();

            List<ScoreEntryM> topPlayers = new ArrayList<>();
            for (Object[] row : resultList) {
                int rank = ((Number) row[0]).intValue();
                String username = (String) row[1];
                int totalScore = ((Number) row[2]).intValue();
                topPlayers.add(new ScoreEntryM(rank, username, totalScore));
            }

            for (ScoreEntryM entry : topPlayers) {
                System.out.println("Rank: " + entry.getRank() +
                        ", Username: " + entry.getUsername() +
                        ", TotalScore: " + entry.getTotalScore());
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
