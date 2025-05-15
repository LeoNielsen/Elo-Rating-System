package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.MonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyStatsRepository extends JpaRepository<MonthlyStats,Long> {
    Optional<MonthlyStats> findByPlayerIdAndMonthAndYear(Long PlayerId, int month, int year);
    Optional<MonthlyStats> findTopByMonthAndYearOrderByMonthlyRatingDesc(int month, int year);
    List<MonthlyStats> findByMonthAndYearAndMonthlyRatingOrderByPlayerAsc(int month, int year, int monthlyRating);
}
