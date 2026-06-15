package EloRatingSystem.Modules.Rating.Services;

import EloRatingSystem.Modules.Matches.Models.TeamMatch;
import EloRatingSystem.Modules.Rating.Dtos.RatingResponseDto;
import EloRatingSystem.Modules.Rating.Models.MonthlyRating;
import EloRatingSystem.Modules.Rating.Repositories.MonthlyRatingRepository;
import EloRatingSystem.Modules.Stats.Dtos.ChartDataDto;
import EloRatingSystem.Modules.Stats.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Modules.Stats.Models.MonthlyStats;
import EloRatingSystem.Modules.Stats.Repositories.MonthlyDailyStatsRepository;
import EloRatingSystem.Modules.Stats.Repositories.MonthlyStatsRepository;
import EloRatingSystem.Modules.Stats.Services.MonthlyStatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyRatingService {

    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;
    @Autowired
    MonthlyDailyStatsRepository monthlyDailyStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    MonthlyStatsService monthlyStatsService;
    @Autowired
    RatingUtils ratingUtils;

    public Mono<List<RatingResponseDto>> getRatingByMatchId(Long id) {
        List<MonthlyRating> ratings = monthlyRatingRepository.findAllByMatchId(id);
        List<RatingResponseDto> dtoList = ratings.stream()
                .map(RatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

    public Mono<List<ChartDataDto>> getChartData() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        List<MonthlyDailyStats> dailyStatsList = monthlyDailyStatsRepository.findAllByMonthAndYear(year,month);
        List<ChartDataDto> chartDataDtoList = dailyStatsList.stream()
                .map(ChartDataDto::new)
                .toList();
        return Mono.just(chartDataDtoList);
    }

    public void newRating(TeamMatch match) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        boolean redWon = match.getRedScore() > match.getBlueScore();
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match, month, year);
    }

    public void newRating(TeamMatch match, int month, int year) {
        boolean redWon = match.getRedScore() > match.getBlueScore();
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match, month, year);
    }

    private void rankingCalculator(Team winner, Team loser, TeamMatch match, int month, int year) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedScore(), match.getBlueScore());

        double winnerOddsAttacker = calculatePlayerOdds(winner.getAttacker(), loser, month, year);
        double winnerOddsDefender = calculatePlayerOdds(winner.getDefender(), loser, month, year);
        double loserOddsAttacker = calculatePlayerOdds(loser.getAttacker(), winner, month, year);
        double loserOddsDefender = calculatePlayerOdds(loser.getDefender(), winner, month, year);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        newMonthlyRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match, month, year);
        newMonthlyRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match, month, year);
        newMonthlyRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match, month, year);
        newMonthlyRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match, month, year);
    }


    protected double calculatePlayerOdds(Player player, Team opponentTeam, int month, int year) {
        MonthlyStats playerStats = getStatsOrDefault(player.getId(), month, year);
        MonthlyStats defenderStats = getStatsOrDefault(opponentTeam.getDefender().getId(), month, year);
        MonthlyStats attackerStats = getStatsOrDefault(opponentTeam.getAttacker().getId(), month, year);

        double oddsAgainstDefender = ratingUtils.calculateOdds(playerStats.getMonthlyRating(), defenderStats.getMonthlyRating(), 500);
        double oddsAgainstAttacker = ratingUtils.calculateOdds(playerStats.getMonthlyRating(), attackerStats.getMonthlyRating(), 500);

        return (oddsAgainstDefender + oddsAgainstAttacker) / 2;
    }

    private MonthlyStats getStatsOrDefault(Long playerId, int month, int year) {
        return monthlyStatsRepository
                .findByPlayerIdAndMonthAndYear(playerId, month, year)
                .orElse(new MonthlyStats(1200));
    }

    private void newMonthlyRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner, TeamMatch match, int month, int year) {
        MonthlyStats monthlyStats = getStatsOrDefault(player.getId(), month, year);
        int oldMonthlyRating = monthlyStats.getMonthlyRating();
        int newMonthlyRating = ratingUtils.calculateNewRating(oldMonthlyRating, pointMultiplier, (teamRating + playerOdds) / 2, isWinner);
        MonthlyRating monthlyRating = new MonthlyRating(match, player, oldMonthlyRating, newMonthlyRating, year, month);
        monthlyRatingRepository.save(monthlyRating);
        monthlyStatsService.updateMonthlyStats(player, monthlyRating, month, year);
        updateMonthlyDailyStats(LocalDate.now(),newMonthlyRating - oldMonthlyRating, player,newMonthlyRating);
    }

    public void updateMonthlyDailyStats(LocalDate date,int ratingChange, Player player, int monthlyRating) {
        monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date)
                .ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            stats.setRating(monthlyRating);
                            monthlyDailyStatsRepository.save(stats);
                        },
                        () -> monthlyDailyStatsRepository.save(new MonthlyDailyStats(player, date, ratingChange, monthlyRating))
                );
    }



    public void deleteRatingsByMatch(LocalDate date,Long id) {
        int year = date.getYear();
        int month = date.getMonthValue();
        List<MonthlyRating> playerRatingList = monthlyRatingRepository.findAllByMatchId(id);
        for (MonthlyRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            MonthlyStats stats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year).orElseThrow();
            stats.setMonthlyRating(rating.getOldRating());
            updateMonthlyDailyStats(date,rating.getOldRating() - rating.getNewRating(), player, rating.getOldRating());
            monthlyStatsRepository.save(stats);
            monthlyRatingRepository.deleteById(rating.getId());
        }
    }

    public int getHighestELOByPlayerId(Long playerId,  int month, int year){
        return monthlyRatingRepository.findTopByPlayerIdAndMonthAndYearOrderByNewRatingDesc(playerId, month,year)
                .map(MonthlyRating::getNewRating)
                .orElse(1200);
    }
    public int getLowestELOByPlayerId(Long playerId, int month, int year){
        return monthlyRatingRepository.findTopByPlayerIdAndMonthAndYearOrderByNewRatingAsc(playerId, month,year)
                .map(MonthlyRating::getNewRating)
                .orElse(1200);
    }
}
