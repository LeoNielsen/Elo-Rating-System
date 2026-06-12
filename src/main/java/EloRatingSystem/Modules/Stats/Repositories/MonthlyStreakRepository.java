package EloRatingSystem.Modules.Stats.Repositories;

import EloRatingSystem.Modules.Stats.Models.Streaks.MonthlyPlayerStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyStreakRepository extends JpaRepository<MonthlyPlayerStreak, Long> {
    Optional<MonthlyPlayerStreak> findTopByPlayerIdAndMonthAndYearOrderByWinStreakDesc(Long playerId, int month, int year);
    Optional<MonthlyPlayerStreak> findTopByPlayerIdAndMonthAndYearOrderByMatchIdDesc(Long playerId, int month, int year);
    void deleteAllByMatchId(Long matchId);
}