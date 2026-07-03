package EloRatingSystem.Modules.Stats.Repositories.Streaks;

import EloRatingSystem.Modules.Stats.Models.Streaks.TeamStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamStreakRepository extends JpaRepository<TeamStreak, Long> {
    Optional<TeamStreak> findTopByTeamPairIdOrderByWinStreakDesc(Long teamPairId);
    Optional<TeamStreak> findTopByTeamPairIdOrderByMatchIdDesc(Long teamPairId);
    void deleteAllByMatchId(Long matchId);
}