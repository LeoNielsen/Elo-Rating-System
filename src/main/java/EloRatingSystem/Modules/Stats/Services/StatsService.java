package EloRatingSystem.Modules.Stats.Services;

import EloRatingSystem.Modules.Achievement.Services.AchievementService;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Rating.Models.PlayerRating;
import EloRatingSystem.Modules.Stats.Models.PlayerStats;
import EloRatingSystem.Modules.Stats.Models.Streaks.PlayerStreak;
import EloRatingSystem.Modules.Stats.Repositories.PlayerStatsRepository;
import EloRatingSystem.Modules.Stats.Repositories.StreakRepository;
import EloRatingSystem.Modules.Stats.Utils.StatsUtils;
import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StatsService {

    @Autowired
    AchievementService achievementService;
    @Autowired
    PlayerStatsRepository statsRepository;

    @Autowired
    StreakRepository streakRepository;
    @Autowired
    StatsUtils statsUtils;


    public void updatePlayerStats(Player player, PlayerRating rating) {
        Match match = rating.getMatch();
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = statsUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);

        int currentStreak = 0;

        Optional<PlayerStats> playerStatsOptional = statsRepository.findByPlayerId(player.getId());
        PlayerStats stats = playerStatsOptional.orElseGet(() ->
                new PlayerStats(
                        player,
                        isAttacker && won ? 1 : 0,
                        !isAttacker && won ? 1 : 0,
                        isAttacker && !won ? 1 : 0,
                        !isAttacker && !won ? 1 : 0,
                        isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                        rating.getNewRating() > rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        rating.getNewRating() < rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0,
                        isBlue && match.getRedTeamScore() == 0 || !isBlue && match.getBlueTeamScore() == 0 ? 1 : 0
                )
        );

        if (playerStatsOptional.isPresent()) {
            if (won) {
                if (statsUtils.tenZeroMatch(match.getBlueTeamScore(), match.getRedTeamScore())) {
                    stats.setShutouts(stats.getShutouts() + 1);
                }
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
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }
        streakRepository.save(new PlayerStreak(match,player,currentStreak));
        statsRepository.save(stats);
        achievementService.checkAndUnlockAchievements(player, match);
    }

    public void undoPlayerStats(Player player, Match match, int highestELO, int lowestELO) {
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = statsUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);

        PlayerStats stats = statsRepository.findByPlayerId(player.getId())
                .orElseThrow(() -> new RuntimeException("Stats missing for player " + player.getNameTag()));

        if (won) {
            if (statsUtils.tenZeroMatch(match.getBlueTeamScore(), match.getRedTeamScore())) {
                stats.setShutouts(stats.getShutouts() - 1);
            }
            if (isAttacker) {
                stats.setAttackerWins(stats.getAttackerWins() - 1);
            } else {
                stats.setDefenderWins(stats.getDefenderWins() - 1);
            }

            stats.setCurrentWinStreak(stats.getCurrentWinStreak() - 1);
            if (stats.getCurrentWinStreak() < 0) stats.setCurrentWinStreak(0);

        } else {
            if (isAttacker) {
                stats.setAttackerLost(stats.getAttackerLost() - 1);
            } else {
                stats.setDefenderLost(stats.getDefenderLost() - 1);
            }
        }

        int goals = isBlue ? match.getBlueTeamScore() : match.getRedTeamScore();
        stats.setGoals(stats.getGoals() - goals);

        stats.setLongestWinStreak(getLongestStreakByPlayerId(player.getId()));
        stats.setCurrentWinStreak(getLatestStreakByPlayerId(player.getId()));

        stats.setHighestELO(highestELO);
        stats.setLowestELO(lowestELO);

        statsRepository.save(stats);
    }

    public int getLongestStreakByPlayerId(Long playerId){
        return streakRepository.findTopByPlayerIdOrderByWinStreakDesc(playerId).getWinStreak();
    }

    public int getLatestStreakByPlayerId(Long playerId){
        return streakRepository.findTopByPlayerIdOrderByMatchIdDesc(playerId).getWinStreak();
    }

    public void deleteStreakByMatchId(Long matchId){
        streakRepository.deleteAllByMatchId(matchId);
    }

}
