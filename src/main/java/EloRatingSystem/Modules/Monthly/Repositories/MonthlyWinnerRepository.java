package EloRatingSystem.Modules.Monthly.Repositories;

import EloRatingSystem.Modules.Monthly.Models.MonthlyWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyWinnerRepository extends JpaRepository<MonthlyWinner,Long> {
    List<MonthlyWinner> findAllByPlayerId(Long playerId);

    List<MonthlyWinner> findByMonthAndYear(int month, int year);
}
