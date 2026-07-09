package EloRatingSystem.Modules.Stats.Repositories;

import EloRatingSystem.Modules.Stats.Dtos.TeamStatisticsResponseDto;
import EloRatingSystem.Modules.Stats.Models.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {
    Optional<TeamStats> findByTeamPairId(Long teamPairId);

    @Query("""
    SELECT new EloRatingSystem.Modules.Stats.Dtos.TeamStatisticsResponseDto(tp, ts, COALESCE(tds.ratingChange, 0))
    FROM TeamPair tp
    JOIN TeamStats ts ON ts.teamPair.id = tp.id
    LEFT JOIN TeamDailyStats tds ON tds.teamPair.id = tp.id AND tds.date = :today
    WHERE tp.id = :teamPairId
""")
    Optional<TeamStatisticsResponseDto> findCombinedStatsByTeamPairIdAndDate(@Param("teamPairId") Long teamPairId, @Param("today") LocalDate today);
}
