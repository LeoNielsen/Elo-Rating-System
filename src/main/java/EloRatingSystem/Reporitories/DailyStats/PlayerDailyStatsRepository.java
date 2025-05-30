package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PlayerDailyStatsRepository extends JpaRepository<PlayerDailyStats,Long> {
        Optional<PlayerDailyStats> findAllByPlayerIdAndDate(Long playerId, Date date);
}
