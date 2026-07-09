package EloRatingSystem.Modules.Rating.Repositories;

import EloRatingSystem.Modules.Rating.Models.TeamRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRatingRepository extends JpaRepository<TeamRating,Long> {
    List<TeamRating> findAllByMatchId(Long id);
    List<TeamRating> findAllByMatchIdAndTeamPairId(Long matchId,Long teamPairId);
    Optional<TeamRating> findTopByTeamPairIdOrderByNewRatingDesc(Long teamPairId);
    Optional<TeamRating> findTopByTeamPairIdOrderByNewRatingAsc(Long teamPairId);
}
