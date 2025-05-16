package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.SoloPlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloRatingRepository extends JpaRepository<SoloPlayerRating,Long> {

    List<SoloPlayerRating> findAllBySoloMatchId(Long id);
    Optional<SoloPlayerRating> findTopMaxNewRatingByPlayerId(Long id);
    Optional<SoloPlayerRating> findTopMinNewRatingByPlayerId(Long id);

    Optional<SoloPlayerRating> findBySoloMatchIdAndPlayerId(Long id, Long id1);
}
