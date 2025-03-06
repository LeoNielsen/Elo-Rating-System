package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.SoloMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloMatchRepository extends JpaRepository<SoloMatch,Long> {
    Optional<SoloMatch> findTop1ByOrderByIdDesc();

    List<SoloMatch> findAllByRedPlayerIdOrBluePlayerId(Long redId,Long blueId);
}
