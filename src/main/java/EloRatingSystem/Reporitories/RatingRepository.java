package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.PlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<PlayerRating,Long> {

    List<PlayerRating> findAllByMatchId(Long id);
    Optional<PlayerRating> findTopMaxNewRatingByPlayerId(Long id);
    Optional<PlayerRating> findTopMinNewRatingByPlayerId(Long id);
}
