package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyRatingRepository extends JpaRepository<MonthlyRating,Long> {
    List<MonthlyRating> findAllByMatchId(Long matchId);
}
