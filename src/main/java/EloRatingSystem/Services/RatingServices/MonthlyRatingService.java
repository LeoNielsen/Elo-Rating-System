package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyRatingRepository;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import EloRatingSystem.Reporitories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MonthlyRatingService {

    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;
    @Autowired
    PlayerRepository playerRepository;
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

        double winnerOddsAttacker = ratingUtils.calculatePlayerOdds(winner.getAttacker(), loser);
        double winnerOddsDefender = ratingUtils.calculatePlayerOdds(winner.getDefender(), loser);
        double loserOddsAttacker = ratingUtils.calculatePlayerOdds(loser.getAttacker(), winner);
        double loserOddsDefender = ratingUtils.calculatePlayerOdds(loser.getDefender(), winner);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        newMonthlyRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match);
        newMonthlyRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match);
        newMonthlyRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match);
        newMonthlyRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match);
    }

    private void newMonthlyRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        int newMonthlyRating = ratingUtils.calculateNewRating(player.getRating(), pointMultiplier, (teamRating + playerOdds) / 2, isWinner);
        MonthlyRating monthlyRating = new MonthlyRating(match, player, player.getRating(), newMonthlyRating);
        monthlyRatingRepository.save(monthlyRating);
        updateMonthlyStats(player, monthlyRating);
        updateMonthlyDailyStats(newMonthlyRating - player.getRating(), player);
        player.setRating(newMonthlyRating);
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

    private void updateMonthlyStats(Player player, MonthlyRating rating) {
        Match match = rating.getMatch();
        boolean isBlue = (player == match.getBlueTeam().getAttacker() || player == match.getBlueTeam().getDefender());
        boolean won = isBlue && match.getBlueTeamScore() > match.getRedTeamScore() || isBlue && match.getRedTeamScore() > match.getBlueTeamScore();
        boolean isAttacker = (player == match.getBlueTeam().getAttacker() || player == match.getRedTeam().getAttacker());

        Optional<MonthlyStats> optionalStats = monthlyStatsRepository.findByPlayerId(player.getId());
        MonthlyStats stats = optionalStats.orElseGet(() ->
                new MonthlyStats(
                        player,
                        isAttacker && won ? 1 : 0,
                        !isAttacker && won ? 1 : 0,
                        isAttacker && !won ? 1 : 0,
                        !isAttacker && !won ? 1 : 0,
                        isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                        rating.getNewRating(),
                        rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0
                )
        );

        if (optionalStats.isPresent()) {
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
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));

            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }

        monthlyStatsRepository.save(stats);
    }

    public void deleteRatingsByMatch(Long Id) {
        List<MonthlyRating> playerRatingList = monthlyRatingRepository.findAllByMatchId(Id);
        for (MonthlyRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setRating(rating.getOldRating());
            updateMonthlyDailyStats(rating.getOldRating() - rating.getNewRating(), player);
            playerRepository.save(player);
            monthlyRatingRepository.deleteById(rating.getId());
        }
    }
}
