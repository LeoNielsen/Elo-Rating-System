package EloRatingSystem.Reporitories.DailyStats;

import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyDailyStatsRepository extends JpaRepository<MonthlyDailyStats,Long> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "player"
    })
    List<MonthlyDailyStats> findAll();
    Optional<MonthlyDailyStats> findAllByPlayerIdAndDate(Long playerId, LocalDate date);
    @Query("SELECT m FROM MonthlyDailyStats m " +
            "WHERE YEAR(m.date) = :year AND MONTH(m.date) = :month")
    List<MonthlyDailyStats> findAllByMonthAndYear(@Param("year") int year,
                                                  @Param("month") int month);

}
