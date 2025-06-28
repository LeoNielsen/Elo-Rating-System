package EloRatingSystem.Reporitories;

import EloRatingSystem.Dtos.PlayerDtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Models.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
   Optional<PlayerStats> findByPlayerId(Long PlayerId);

   @Query("""
    SELECT new EloRatingSystem.Dtos.PlayerDtos.PlayerStatisticsResponseDto(p, ps, COALESCE(pds.ratingChange, 0))
    FROM Player p
    JOIN PlayerStats ps ON ps.player.id = p.id
    LEFT JOIN PlayerDailyStats pds ON pds.player.id = p.id AND pds.date = :today
    WHERE p.id = :playerId
""")
   Optional<PlayerStatisticsResponseDto> findCombinedStatsByPlayerIdAndDate(@Param("playerId") Long playerId, @Param("today") LocalDate today);
}
