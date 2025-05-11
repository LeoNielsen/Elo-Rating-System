package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.PlayerRating;
import EloRatingSystem.Models.SoloPlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloRatingRepository extends JpaRepository<SoloPlayerRating,Long> {

    List<SoloPlayerRating> findAllBySoloMatchId(Long id);
    Optional<PlayerRating> findTopMaxNewRatingByPlayerId(Long id);
    Optional<PlayerRating> findTopMinNewRatingByPlayerId(Long id);
}
