package EloRatingSystem.Modules.Stats.Repositories.Streaks;

import EloRatingSystem.Modules.Stats.Models.Streaks.PlayerStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<PlayerStreak, Long> {
    Optional<PlayerStreak> findTopByPlayerIdOrderByWinStreakDesc(Long playerId);
    Optional<PlayerStreak> findTopByPlayerIdOrderByMatchIdDesc(Long playerId);
    void deleteAllByMatchId(Long matchId);
}