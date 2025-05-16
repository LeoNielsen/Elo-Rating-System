package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyRatingRepository extends JpaRepository<MonthlyRating,Long> {
    List<MonthlyRating> findAllByMatchId(Long matchId);
    Optional<MonthlyRating> findByMatchIdAndPlayerId(Long matchId,Long playerId);


    Optional<MonthlyRating> findTopByPlayerIdOrderByNewRatingAsc(Long id);

    Optional<MonthlyRating> findTopByPlayerIdOrderByNewRatingDesc(Long id);
}
