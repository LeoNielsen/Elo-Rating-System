package EloRatingSystem.Modules.Rating.Repositories;

import EloRatingSystem.Modules.Rating.Models.MonthlyRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyRatingRepository extends JpaRepository<MonthlyRating,Long> {
    List<MonthlyRating> findAllByMatchId(Long matchId);
    List<MonthlyRating> findAllByMatchIdAndPlayerId(Long matchId,Long playerId);

    Optional<MonthlyRating> findTopByPlayerIdAndMonthAndYearOrderByNewRatingAsc(Long id, int month, int year);

    Optional<MonthlyRating> findTopByPlayerIdAndMonthAndYearOrderByNewRatingDesc(Long id, int month, int year);
}
