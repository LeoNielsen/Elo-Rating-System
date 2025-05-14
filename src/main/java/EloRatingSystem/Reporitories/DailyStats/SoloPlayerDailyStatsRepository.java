package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface SoloPlayerDailyStatsRepository extends JpaRepository<SoloPlayerDailyStats,Long> {
    Optional<SoloPlayerDailyStats> findAllByPlayerIdAndDate(Long playerId, Date date);
}
