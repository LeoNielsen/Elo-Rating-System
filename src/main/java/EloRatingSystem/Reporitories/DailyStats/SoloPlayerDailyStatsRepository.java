package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoloPlayerDailyStatsRepository extends JpaRepository<SoloPlayerDailyStats,Long> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "player"
    })
    List<SoloPlayerDailyStats> findAll();
    Optional<SoloPlayerDailyStats> findAllByPlayerIdAndDate(Long playerId, LocalDate date);
}
