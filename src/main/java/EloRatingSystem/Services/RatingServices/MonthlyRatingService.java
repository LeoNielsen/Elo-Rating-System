package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyRatingRepository;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
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

    public void newRating(Match match) {
        boolean redWon = match.getRedTeamScore() > match.getBlueTeamScore();
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match);
    }

    private void rankingCalculator(Team winner, Team loser, Match match) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        double winnerOddsAttacker = calculatePlayerOdds(winner.getAttacker(), loser);
        double winnerOddsDefender = calculatePlayerOdds(winner.getDefender(), loser);
        double loserOddsAttacker = calculatePlayerOdds(loser.getAttacker(), winner);
        double loserOddsDefender = calculatePlayerOdds(loser.getDefender(), winner);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        newMonthlyRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match);
        newMonthlyRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match);
        newMonthlyRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match);
        newMonthlyRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match);
    }


    protected double calculatePlayerOdds(Player player, Team opponentTeam) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        Optional<MonthlyStats> optionalStats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
        Optional<MonthlyStats> optDefender = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(opponentTeam.getDefender().getId(), month, year);
        Optional<MonthlyStats> optAttacker = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(opponentTeam.getAttacker().getId(), month, year);
        MonthlyStats stats = optionalStats.orElseGet(() ->
                new MonthlyStats(1200)
        );
        MonthlyStats attacker = optionalStats.orElseGet(() ->
                new MonthlyStats(1200)
        );
        MonthlyStats defender = optionalStats.orElseGet(() ->
                new MonthlyStats(1200)
        );
        double oddsAgainstAttacker = ratingUtils.calculateOdds(stats.getMonthlyRating(), defender.getMonthlyRating(), 500);
        double oddsAgainstDefender = ratingUtils.calculateOdds(stats.getMonthlyRating(), attacker.getMonthlyRating(), 500);
        return (oddsAgainstAttacker + oddsAgainstDefender) / 2;
    }

    private void newMonthlyRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        boolean isBlue = (player == match.getBlueTeam().getAttacker() || player == match.getBlueTeam().getDefender());
        boolean won = isBlue && match.getBlueTeamScore() > match.getRedTeamScore() || isBlue && match.getRedTeamScore() > match.getBlueTeamScore();
        boolean isAttacker = (player == match.getBlueTeam().getAttacker() || player == match.getRedTeam().getAttacker());

        int newRating;
        MonthlyStats stats;
        MonthlyRating monthlyRating;
        Optional<MonthlyStats> optionalStats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
        if (optionalStats.isPresent()) {
            stats = optionalStats.get();
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
            newRating = ratingUtils.calculateNewRating(stats.getMonthlyRating(), pointMultiplier, (teamRating + playerOdds) / 2, isWinner);
            monthlyRating = new MonthlyRating(match,player,stats.getMonthlyRating(),newRating);
            stats.setMonthlyRating(newRating);
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        } else {
            int defaultRating = 1200;
            newRating = ratingUtils.calculateNewRating(defaultRating, pointMultiplier, (teamRating + playerOdds) / 2, isWinner);
            monthlyRating = new MonthlyRating(match, player, defaultRating, newRating);
            stats = new MonthlyStats(
                    player,
                    year,
                    month,
                    newRating,
                    isAttacker && won ? 1 : 0,
                    !isAttacker && won ? 1 : 0,
                    isAttacker && !won ? 1 : 0,
                    !isAttacker && !won ? 1 : 0,
                    isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                    newRating,
                    newRating,
                    won ? 1 : 0,
                    won ? 1 : 0
            );
        }

        updateMonthlyDailyStats(newRating - monthlyRating.getOldRating(), player);
        monthlyRatingRepository.save(monthlyRating);
        monthlyStatsRepository.save(stats);
    }

    private void updateMonthlyDailyStats(int ratingChange, Player player) {
        Date today = new Date(System.currentTimeMillis());
        monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today)
                .ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            monthlyDailyStatsRepository.save(stats);
                        },
                        () -> monthlyDailyStatsRepository.save(new MonthlyDailyStats(player, today, ratingChange))
                );
    }

    public void deleteRatingsByMatch(Long Id) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        List<MonthlyRating> playerRatingList = monthlyRatingRepository.findAllByMatchId(Id);
        for (MonthlyRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            MonthlyStats stats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year).orElseThrow();
            stats.setMonthlyRating(rating.getOldRating());
            updateMonthlyDailyStats(rating.getOldRating() - rating.getNewRating(), player);
            monthlyRatingRepository.deleteById(rating.getId());
        }
    }
}
