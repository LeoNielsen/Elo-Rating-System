package EloRatingSystem.Modules.Rating.Repositories;

import EloRatingSystem.Modules.Rating.Models.PlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<PlayerRating,Long> {

    List<PlayerRating> findAllByMatchId(Long id);
    List<PlayerRating> findAllByMatchIdAndPlayerId(Long matchId,Long playerId);
    PlayerRating findTopByPlayerIdOrderByNewRatingDesc(Long playerId);
    PlayerRating findTopByPlayerIdOrderByNewRatingAsc(Long playerId);
}
