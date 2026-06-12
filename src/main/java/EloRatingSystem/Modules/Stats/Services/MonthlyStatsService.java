package EloRatingSystem.Modules.Stats.Services;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Rating.Models.MonthlyRating;
import EloRatingSystem.Modules.Stats.Models.MonthlyStats;
import EloRatingSystem.Modules.Stats.Models.Streaks.MonthlyPlayerStreak;
import EloRatingSystem.Modules.Stats.Repositories.MonthlyStatsRepository;
import EloRatingSystem.Modules.Stats.Repositories.MonthlyStreakRepository;
import EloRatingSystem.Modules.Stats.Utils.StatsUtils;
import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MonthlyStatsService {

    @Autowired
    StatsUtils statsUtils;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    MonthlyStreakRepository monthlyStreakRepository;

    public void updateMonthlyStats(Player player, MonthlyRating rating, int month, int year) {
        Match match = rating.getMatch();
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = statsUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);

        int currentStreak = 0;

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
                currentStreak = stats.getCurrentWinStreak() + 1;
                stats.setCurrentWinStreak(currentStreak);
                stats.setLongestWinStreak(Math.max(stats.getLongestWinStreak(), stats.getCurrentWinStreak()));
            } else {
                if (isAttacker) {
                    stats.setAttackerLost(stats.getAttackerLost() + 1);
                } else {
                    stats.setDefenderLost(stats.getDefenderLost() + 1);
                }
                stats.setCurrentWinStreak(currentStreak);
            }

            int newRating = rating.getNewRating();
            stats.setMonthlyRating(rating.getNewRating());
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }

        monthlyStreakRepository.save(new MonthlyPlayerStreak(match, year, month, player, currentStreak));
        monthlyStatsRepository.save(stats);
    }

    public void undoPlayerStats(Player player, Match match, int highestELO, int lowestELO, int month, int year) {
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = statsUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);

        MonthlyStats stats = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year)
                .orElseThrow(() -> new RuntimeException("Stats missing for player " + player.getNameTag()));

        if (won) {
            if (isAttacker) {
                stats.setAttackerWins(stats.getAttackerWins() - 1);
            } else {
                stats.setDefenderWins(stats.getDefenderWins() - 1);
            }
        } else {
            if (isAttacker) {
                stats.setAttackerLost(stats.getAttackerLost() - 1);
            } else {
                stats.setDefenderLost(stats.getDefenderLost() - 1);
            }
        }

        int goals = isBlue ? match.getBlueTeamScore() : match.getRedTeamScore();
        stats.setGoals(stats.getGoals() - goals);

        stats.setLongestWinStreak(getLongestStreakByPlayerId(player.getId(), month, year));
        stats.setCurrentWinStreak(getLatestStreakByPlayerId(player.getId(), month, year));

        stats.setHighestELO(highestELO);
        stats.setLowestELO(lowestELO);

        monthlyStatsRepository.save(stats);
    }

    public int getLongestStreakByPlayerId(Long playerId, int month, int year) {
        return monthlyStreakRepository
                .findTopByPlayerIdAndMonthAndYearOrderByWinStreakDesc(playerId, month, year)
                .map(MonthlyPlayerStreak::getWinStreak)
                .orElse(0);
    }

    public int getLatestStreakByPlayerId(Long playerId, int month, int year) {
        return monthlyStreakRepository
                .findTopByPlayerIdAndMonthAndYearOrderByMatchIdDesc(playerId, month, year)
                .map(MonthlyPlayerStreak::getWinStreak)
                .orElse(0);
    }


    public void deleteStreakByMatchId(Long matchId) {
        monthlyStreakRepository.deleteAllByMatchId(matchId);
    }
}
