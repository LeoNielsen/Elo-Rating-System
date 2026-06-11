package EloRatingSystem.Modules.Stats.Repositories;

import EloRatingSystem.Modules.Stats.Models.Streaks.PlayerStreak;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyStreakRepository extends JpaRepository<PlayerStreak, Long> {
    PlayerStreak findTopByPlayerIdOrderByWinStreakDesc(Long playerId);
    PlayerStreak findTopByPlayerIdOrderByMatchIdDesc(Long playerId);
    void deleteAllByMatchId(Long matchId);
}