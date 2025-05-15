package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyWinnerRepository extends JpaRepository<MonthlyWinner,Long> {

}
