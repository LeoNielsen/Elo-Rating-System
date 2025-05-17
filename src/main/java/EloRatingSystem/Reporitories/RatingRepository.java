package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.PlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<PlayerRating,Long> {

    List<PlayerRating> findAllByMatchId(Long id);
    List<PlayerRating> findAllByMatchIdAndPlayerId(Long matchId,Long playerId);
}
