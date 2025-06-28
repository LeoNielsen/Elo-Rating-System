package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerDailyStatsRepository extends JpaRepository<PlayerDailyStats,Long> {
        @Override
        @NonNull
        @EntityGraph(attributePaths = {
                "player"
        })
        List<PlayerDailyStats> findAll();
        Optional<PlayerDailyStats> findAllByPlayerIdAndDate(Long playerId, LocalDate date);
}
