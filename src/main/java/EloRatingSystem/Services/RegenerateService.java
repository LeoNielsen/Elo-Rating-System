package EloRatingSystem.Services;

import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.PlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.SoloPlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.*;
import EloRatingSystem.Services.RatingServices.MonthlyRatingService;
import EloRatingSystem.Services.RatingServices.RatingService;
import EloRatingSystem.Services.RatingServices.SoloRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RegenerateService {

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PlayerStatsRepository playerStatsRepository;
    @Autowired
    PlayerDailyStatsRepository playerDailyStatsRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    MatchRepository matchRepository;
    @Autowired
    RatingService ratingService;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    MonthlyDailyStatsRepository monthlyDailyStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    SoloPlayerStatsRepository soloPlayerStatsRepository;
    @Autowired
    SoloPlayerDailyStatsRepository soloPlayerDailyStatsRepository;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    SoloRatingRepository soloRatingRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    AchievementService achievementService;

    public void playerStatisticsGenAll() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            regeneratePlayerStatistics(player);
        }
    }

    public void regeneratePlayerStatistics(Player player) {
        Date today = new Date(System.currentTimeMillis());
        Optional<PlayerDailyStats> dailyStats = playerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today);
        dailyStats.ifPresent(stats -> playerDailyStatsRepository.delete(stats));

        Optional<PlayerStats> statsOpt = playerStatsRepository.findByPlayerId(player.getId());
        statsOpt.ifPresent(stats -> playerStatsRepository.delete(stats));

        List<Match> matches = getMatchesForPlayer(player);
        matches.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matches) {
            List<PlayerRating> ratings = ratingRepository.findAllByMatchIdAndPlayerId(match.getId(), player.getId());
            PlayerRating rating = ratings.get(0);
            ratingService.updatePlayerStats(player, rating);
            if (match.getDate().toLocalDate().equals(today.toLocalDate())) {
                ratingService.updatePlayerDailyStats(rating.getNewRating() - rating.getOldRating(), player);
            }
            achievementService.checkAndUnlockAchievements(player,match);
        }
    }

    public void regenerateSoloPlayerStatisticsAll() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            regenerateSoloPlayerStatistics(player);
        }
    }

    public void regenerateSoloPlayerStatistics(Player player) {
        Date today = new Date(System.currentTimeMillis());
        Optional<SoloPlayerDailyStats> dailyStats = soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today);
        dailyStats.ifPresent(stats -> soloPlayerDailyStatsRepository.delete(stats));

        Optional<SoloPlayerStats> statsOpt = soloPlayerStatsRepository.findByPlayerId(player.getId());
        statsOpt.ifPresent(stats -> soloPlayerStatsRepository.delete(stats));

        List<SoloMatch> matches = soloMatchRepository.findAllByRedPlayerIdOrBluePlayerId(player.getId(), player.getId());
        matches.sort(Comparator.comparingLong(SoloMatch::getId));

        for (SoloMatch match : matches) {
            SoloPlayerRating rating = soloRatingRepository
                    .findBySoloMatchIdAndPlayerId(match.getId(), player.getId()).orElseThrow();
            soloRatingService.updatePlayerStats(player, rating);
            if (match.getDate().toLocalDate().equals(today.toLocalDate())) {
                soloRatingService.updatePlayerDailyStats(rating.getNewRating() - rating.getOldRating(), player);
            }
            achievementService.checkAndUnlockAchievementsSolo(player,match);
        }
    }

    public void monthlyStatisticsGenAll() {
        monthlyStatsRepository.deleteAll();
        monthlyRatingRepository.deleteAll();

        List<Match> matches = matchRepository.findAll();
        matches.sort(Comparator.comparingLong(Match::getId));
        for (Match match : matches) {
            LocalDate date = match.getDate().toLocalDate();
            int month = date.getMonthValue();
            int year = date.getYear();
            monthlyRatingService.newRating(match, month, year);
        }

        monthlyDailyStatsRepository.deleteAll();

        Date date = new Date(System.currentTimeMillis());
        List<Match> matchesToday = matchRepository.findAllByDate(date);
        matchesToday.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matchesToday) {
            List<MonthlyRating> ratings = monthlyRatingRepository.findAllByMatchId(match.getId());
            for (MonthlyRating rating : ratings) {
                monthlyRatingService.updateMonthlyDailyStats(rating.getNewRating() - rating.getOldRating(), rating.getPlayer());
            }
        }
    }

    public void regenerateMonthlyStatistics(Player player) {
        Date today = new Date(System.currentTimeMillis());
        LocalDate localToday = today.toLocalDate();
        int month = localToday.getMonthValue();
        int year = localToday.getYear();

        Optional<MonthlyDailyStats> dailyStats = monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today);
        dailyStats.ifPresent(stats -> monthlyDailyStatsRepository.delete(stats));

        Optional<MonthlyStats> statsOpt = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
        statsOpt.ifPresent(stats -> monthlyStatsRepository.delete(stats));

        List<Match> matches = getMatchesForPlayer(player);
        matches.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matches) {
            LocalDate date = match.getDate().toLocalDate();
            if (date.getMonthValue() == month && date.getYear() == year) {
                List<MonthlyRating> ratings = monthlyRatingRepository
                        .findAllByMatchIdAndPlayerId(match.getId(), player.getId());
                for (MonthlyRating rating : ratings) {
                    monthlyRatingService.updateMonthlyStats(player, rating, month, year);
                    if (match.getDate().toLocalDate().equals(today.toLocalDate())) {
                        monthlyRatingService.updateMonthlyDailyStats(rating.getNewRating() - rating.getOldRating(), player);
                    }
                }
            }
        }
    }

    private List<Match> getMatchesForPlayer(Player player) {
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());
        List<Match> matches = new ArrayList<>();
        for (Team team : teams) {
            matches.addAll(matchRepository.findAllByRedTeamIdOrBlueTeamId(team.getId(), team.getId()));
        }
        return matches;
    }

}
