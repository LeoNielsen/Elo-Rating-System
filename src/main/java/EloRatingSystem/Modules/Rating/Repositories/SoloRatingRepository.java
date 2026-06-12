package EloRatingSystem.Modules.Rating.Repositories;

import EloRatingSystem.Modules.Rating.Models.SoloPlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloRatingRepository extends JpaRepository<SoloPlayerRating,Long> {

    List<SoloPlayerRating> findAllBySoloMatchId(Long id);

    Optional<SoloPlayerRating> findBySoloMatchIdAndPlayerId(Long id, Long id1);

    Optional<SoloPlayerRating> findTopByPlayerIdOrderByNewRatingDesc(Long playerId);

    Optional<SoloPlayerRating> findTopByPlayerIdOrderByNewRatingAsc(Long playerId);
}
