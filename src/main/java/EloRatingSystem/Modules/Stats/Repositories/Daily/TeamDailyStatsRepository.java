package EloRatingSystem.Modules.Stats.Repositories.Daily;

import EloRatingSystem.Modules.Stats.Models.DailyStats.TeamDailyStats;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamDailyStatsRepository extends JpaRepository<TeamDailyStats,Long> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "teamPair"
    })
    List<TeamDailyStats> findAll();
    Optional<TeamDailyStats> findAllByTeamPairIdAndDate(Long teamPairId, LocalDate date);
}