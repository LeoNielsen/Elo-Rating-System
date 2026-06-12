package EloRatingSystem.Modules.Stats.Repositories;

import EloRatingSystem.Modules.Stats.Models.Streaks.SoloStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoloStreakRepository extends JpaRepository<SoloStreak, Long> {
    Optional<SoloStreak> findTopByPlayerIdOrderByWinStreakDesc(Long playerId);
    Optional<SoloStreak> findTopByPlayerIdOrderByMatchIdDesc(Long playerId);
    void deleteAllByMatchId(Long matchId);
}