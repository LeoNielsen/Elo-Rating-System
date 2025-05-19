package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyWinnerRepository extends JpaRepository<MonthlyWinner,Long> {
    List<MonthlyWinner> findAllByPlayerId(Long playerId);
}
