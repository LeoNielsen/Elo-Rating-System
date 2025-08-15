package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.PlayerDtos.ChartDataDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyRatingRepository;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MonthlyRatingService {

    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;
    @Autowired
    MonthlyDailyStatsRepository monthlyDailyStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
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

    public void newRating(Match match) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        boolean redWon = match.getRedTeamScore() > match.getBlueTeamScore();
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match, month, year);
    }

    public void newRating(Match match, int month, int year) {
        boolean redWon = match.getRedTeamScore() > match.getBlueTeamScore();
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match, month, year);
    }

    private void rankingCalculator(Team winner, Team loser, Match match, int month, int year) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

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

    private void newMonthlyRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner, Match match, int month, int year) {
        MonthlyStats monthlyStats = getStatsOrDefault(player.getId(), month, year);
        int oldMonthlyRating = monthlyStats.getMonthlyRating();
        int newMonthlyRating = ratingUtils.calculateNewRating(oldMonthlyRating, pointMultiplier, (teamRating + playerOdds) / 2, isWinner);
        MonthlyRating monthlyRating = new MonthlyRating(match, player, oldMonthlyRating, newMonthlyRating);
        monthlyRatingRepository.save(monthlyRating);
        updateMonthlyStats(player, monthlyRating, month, year);
        updateMonthlyDailyStats(newMonthlyRating - oldMonthlyRating, player,newMonthlyRating);
    }

    public void updateMonthlyDailyStats(int ratingChange, Player player, int monthlyRating) {
        LocalDate today = LocalDate.now();
        monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today)
                .ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            stats.setRating(monthlyRating);
                            monthlyDailyStatsRepository.save(stats);
                        },
                        () -> monthlyDailyStatsRepository.save(new MonthlyDailyStats(player, today, ratingChange, monthlyRating))
                );
    }

    public void updateMonthlyStats(Player player, MonthlyRating rating, int month, int year) {
        Match match = rating.getMatch();
        boolean isBlue = ratingUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = ratingUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = ratingUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);


        Optional<MonthlyStats> statsOpt = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
        MonthlyStats stats = statsOpt.orElseGet(() ->
                new MonthlyStats(
                        player,
                        year,
                        month,
                        rating.getNewRating(),
                        isAttacker && won ? 1 : 0,
                        !isAttacker && won ? 1 : 0,
                        isAttacker && !won ? 1 : 0,
                        !isAttacker && !won ? 1 : 0,
                        isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                        rating.getNewRating() > rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        rating.getNewRating() < rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0
                )
        );

        if (statsOpt.isPresent()) {
            if (won) {
                if (isAttacker) {
                    stats.setAttackerWins(stats.getAttackerWins() + 1);
                } else {
                    stats.setDefenderWins(stats.getDefenderWins() + 1);
                }
                stats.setCurrentWinStreak(stats.getCurrentWinStreak() + 1);
                stats.setLongestWinStreak(Math.max(stats.getLongestWinStreak(), stats.getCurrentWinStreak()));
            } else {
                if (isAttacker) {
                    stats.setAttackerLost(stats.getAttackerLost() + 1);
                } else {
                    stats.setDefenderLost(stats.getDefenderLost() + 1);
                }
                stats.setCurrentWinStreak(0);
            }

            int newRating = rating.getNewRating();
            stats.setMonthlyRating(rating.getNewRating());
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }
        monthlyStatsRepository.save(stats);
    }

    public void deleteRatingsByMatch(LocalDate date,Long id) {
        int year = date.getYear();
        int month = date.getMonthValue();
        List<MonthlyRating> playerRatingList = monthlyRatingRepository.findAllByMatchId(id);
        for (MonthlyRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            MonthlyStats stats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year).orElseThrow();
            stats.setMonthlyRating(rating.getOldRating());
            updateMonthlyDailyStats(rating.getOldRating() - rating.getNewRating(), player, rating.getOldRating());
            monthlyRatingRepository.deleteById(rating.getId());
        }
    }
}
