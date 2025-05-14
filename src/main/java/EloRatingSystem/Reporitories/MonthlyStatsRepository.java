package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyStatsRepository extends JpaRepository<MonthlyStats,Long> {
    Optional<MonthlyStats> findByPlayerId(Long PlayerId);
}
