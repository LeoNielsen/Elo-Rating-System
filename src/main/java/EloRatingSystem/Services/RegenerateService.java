package EloRatingSystem.Services;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import EloRatingSystem.Modules.Rating.Models.MonthlyRating;
import EloRatingSystem.Modules.Rating.Models.PlayerRating;
import EloRatingSystem.Modules.Rating.Models.SoloPlayerRating;
import EloRatingSystem.Modules.Rating.Repositories.MonthlyRatingRepository;
import EloRatingSystem.Modules.Rating.Repositories.RatingRepository;
import EloRatingSystem.Modules.Rating.Repositories.SoloRatingRepository;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import EloRatingSystem.Modules.Stats.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Modules.Stats.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Modules.Stats.Models.PlayerStats;
import EloRatingSystem.Modules.Stats.Models.SoloPlayerStats;
import EloRatingSystem.Modules.Stats.Repositories.*;
import EloRatingSystem.Modules.Stats.Services.MonthlyStatsService;
import EloRatingSystem.Modules.Stats.Services.StatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
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
    StatsService statsService;
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
    MonthlyStatsService monthlyStatsService;
    @Autowired
    StreakRepository streakRepository;
    @Autowired
    MonthlyStreakRepository monthlyStreakRepository;
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

    public void playerStatisticsGenAll() {
        List<Player> players = playerRepository.findAll();
        playerDailyStatsRepository.deleteAll();
        streakRepository.deleteAll();
        for (Player player : players) {
            regeneratePlayerStatistics(player);
        }
    }

    public void regeneratePlayerStatistics(Player player) {
        Optional<PlayerDailyStats> dailyStats = playerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), LocalDate.now());
        dailyStats.ifPresent(stats -> playerDailyStatsRepository.delete(stats));

        Optional<PlayerStats> statsOpt = playerStatsRepository.findByPlayerId(player.getId());
        statsOpt.ifPresent(stats -> playerStatsRepository.delete(stats));

        List<Match> matches = getMatchesForPlayer(player);
        matches.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matches) {
            List<PlayerRating> ratings = ratingRepository.findAllByMatchIdAndPlayerId(match.getId(), player.getId());
            PlayerRating rating = ratings.get(0);
            statsService.updatePlayerStats(player, rating);
            ratingService.updatePlayerDailyStats(match.getDate().toLocalDate(), rating.getNewRating() - rating.getOldRating(), player, rating.getNewRating());
        }
    }

    public void regenerateSoloPlayerStatisticsAll() {
        List<Player> players = playerRepository.findAll();
        soloPlayerDailyStatsRepository.deleteAll();
        for (Player player : players) {
            regenerateSoloPlayerStatistics(player);
        }
    }

    public void regenerateSoloPlayerStatistics(Player player) {
        LocalDate today = LocalDate.now();
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
            soloRatingService.updatePlayerDailyStats(match.getDate().toLocalDate(), rating.getNewRating() - rating.getOldRating(), player, rating.getNewRating());
        }
    }

    public void monthlyStatisticsGenAll() {
        monthlyStatsRepository.deleteAll();
        monthlyRatingRepository.deleteAll();
        monthlyStreakRepository.deleteAll();

        List<Match> matches = matchRepository.findAll();
        matches.sort(Comparator.comparingLong(Match::getId));
        for (Match match : matches) {
            LocalDate date = match.getDate().toLocalDate();
            int month = date.getMonthValue();
            int year = date.getYear();
            monthlyRatingService.newRating(match, month, year);
        }

        monthlyDailyStatsRepository.deleteAll();

        YearMonth yearMonth = YearMonth.now();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Match> matchesToday = matchRepository.findAllByDateBetween(Date.valueOf(start),
                Date.valueOf(end));
        matchesToday.sort(Comparator.comparingLong(Match::getId));

        LocalDate today = LocalDate.now();

        for (Match match : matchesToday) {
            List<MonthlyRating> ratings = monthlyRatingRepository.findAllByMatchId(match.getId());
            for (MonthlyRating rating : ratings) {
                if (match.getDate().toLocalDate().equals(today)) {
                    monthlyRatingService.updateMonthlyDailyStats(match.getDate().toLocalDate(), rating.getNewRating() - rating.getOldRating(), rating.getPlayer(), rating.getNewRating());
                }
            }
        }
    }

//    public void regenerateMonthlyStatistics(Player player) {
//        LocalDate today = LocalDate.now();
//        int month = today.getMonthValue();
//        int year = today.getYear();
//
//        Optional<MonthlyDailyStats> dailyStats = monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today);
//        dailyStats.ifPresent(stats -> monthlyDailyStatsRepository.delete(stats));
//
//        Optional<MonthlyStats> statsOpt = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
//        statsOpt.ifPresent(stats -> monthlyStatsRepository.delete(stats));
//
//        List<Match> matches = getMatchesForPlayer(player);
//        matches.sort(Comparator.comparingLong(Match::getId));
//
//        for (Match match : matches) {
//            LocalDate date = match.getDate().toLocalDate();
//            if (date.getMonthValue() == month && date.getYear() == year) {
//                List<MonthlyRating> ratings = monthlyRatingRepository
//                        .findAllByMatchIdAndPlayerId(match.getId(), player.getId());
//                for (MonthlyRating rating : ratings) {
//                    monthlyStatsService.updateMonthlyStats(player, rating, month, year);
//                    if (match.getDate().toLocalDate().getMonth().equals(today.getMonth())) {
//                        monthlyRatingService.updateMonthlyDailyStats(match.getDate().toLocalDate(),rating.getNewRating() - rating.getOldRating(), player, rating.getNewRating());
//                    }
//                }
//            }
//        }
//    }

    private List<Match> getMatchesForPlayer(Player player) {
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());
        List<Match> matches = new ArrayList<>();
        for (Team team : teams) {
            matches.addAll(matchRepository.findAllByRedTeamIdOrBlueTeamId(team.getId(), team.getId()));
        }
        return matches;
    }

}
