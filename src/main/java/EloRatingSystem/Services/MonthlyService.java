package EloRatingSystem.Services;

import EloRatingSystem.Models.MonthlyStats;
import EloRatingSystem.Models.MonthlyWinner;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyWinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MonthlyService {

    @Autowired
    MonthlyWinnerRepository monthlyWinnerRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    AchievementService achievementService;

    @Scheduled(cron = "0 0 2 1 * *")
    public void setMonthlyWinner() {
        System.out.println("run");
        LocalDate today = LocalDate.now();
        LocalDate previousMonthDate = today.minusMonths(1);
        int month = previousMonthDate.getMonthValue();
        int year = previousMonthDate.getYear();

        Optional<MonthlyStats> topStatOpt = monthlyStatsRepository
                .findTopByMonthAndYearOrderByMonthlyRatingDesc(month, year);
        if (topStatOpt.isEmpty()) {
            return;
        }
        int topRating = topStatOpt.get().getMonthlyRating();

        List<MonthlyStats> topPlayers = monthlyStatsRepository
                .findByMonthAndYearAndMonthlyRatingOrderByPlayerAsc(month, year, topRating);

        for (MonthlyStats stats : topPlayers) {
            monthlyWinnerRepository.save(new MonthlyWinner(
                    stats.getPlayer(),
                    year,
                    month,
                    stats.getMonthlyRating()
            ));
            achievementService.checkAndUnlockAchievementsMonthly(stats.getPlayer());
        }
    }
}
