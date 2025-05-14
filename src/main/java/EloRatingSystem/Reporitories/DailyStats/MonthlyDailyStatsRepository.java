package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MonthlyDailyStatsRepository extends JpaRepository<MonthlyDailyStats,Long> {
    Optional<MonthlyDailyStats> findAllByPlayerIdAndDate(Long playerId, Date date);
}
