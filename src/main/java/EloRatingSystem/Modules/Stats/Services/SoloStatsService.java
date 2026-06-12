package EloRatingSystem.Modules.Stats.Services;

import EloRatingSystem.Modules.Achievement.Services.AchievementService;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Rating.Models.SoloPlayerRating;
import EloRatingSystem.Modules.Stats.Models.SoloPlayerStats;
import EloRatingSystem.Modules.Stats.Models.Streaks.SoloStreak;
import EloRatingSystem.Modules.Stats.Repositories.SoloPlayerStatsRepository;
import EloRatingSystem.Modules.Stats.Repositories.SoloStreakRepository;
import EloRatingSystem.Modules.Stats.Utils.StatsUtils;
import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SoloStatsService {

    @Autowired
    SoloPlayerStatsRepository soloPlayerStatsRepository;
    @Autowired
    SoloStreakRepository soloStreakRepository;
    @Autowired
    AchievementService achievementService;
    @Autowired
    StatsUtils statsUtils;

    public void updatePlayerStats(Player player, SoloPlayerRating rating) {
        SoloMatch match = rating.getSoloMatch();
        boolean isBlue = match.getBluePlayer() == player;
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueScore(), match.getRedScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;

        int currentStreak = 0;

        Optional<SoloPlayerStats> playerStatsOptional = soloPlayerStatsRepository.findByPlayerId(player.getId());
        SoloPlayerStats stats = playerStatsOptional.orElseGet(() ->
                new SoloPlayerStats(
                        player,
                        won ? 1 : 0,
                        !won ? 1 : 0,
                        isBlue ? match.getBlueScore() : match.getRedScore(),
                        rating.getNewRating() > rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        rating.getNewRating() < rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0,
                        isBlue && match.getRedScore() == 0 || !isBlue && match.getBlueScore() == 0 ? 1 : 0
                )
        );

        if (playerStatsOptional.isPresent()) {
            if (won) {
                if (statsUtils.tenZeroMatch(match.getBlueScore(), match.getRedScore())) {
                    stats.setShutouts(stats.getShutouts() + 1);
                }
                stats.setWins(stats.getWins() + 1);
                currentStreak = stats.getCurrentWinStreak() + 1;
                stats.setCurrentWinStreak(currentStreak);
                if (stats.getCurrentWinStreak() > stats.getLongestWinStreak()) {
                    stats.setLongestWinStreak(stats.getCurrentWinStreak());
                }
            } else {
                stats.setLost(stats.getLost() + 1);
                stats.setCurrentWinStreak(currentStreak);
            }

            int newRating = rating.getNewRating();
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueScore() : match.getRedScore()));
        }
        soloStreakRepository.save(new SoloStreak(match,player,currentStreak));
        soloPlayerStatsRepository.save(stats);
        achievementService.checkAndUnlockAchievementsSolo(player, match);
    }

    public void undoPlayerStats(Player player, SoloMatch match, int highestELO, int lowestELO) {
        boolean isBlue = match.getBluePlayer() == player;
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueScore(), match.getRedScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;

        SoloPlayerStats stats = soloPlayerStatsRepository.findByPlayerId(player.getId())
                .orElseThrow(() -> new RuntimeException("Stats missing for player " + player.getNameTag()));

        if (won) {
            if (statsUtils.tenZeroMatch(match.getBlueScore(), match.getRedScore())) {
                stats.setShutouts(stats.getShutouts() - 1);
            }
            stats.setWins(stats.getWins() - 1);
        } else {
            stats.setLost(stats.getLost() - 1);
        }

        int goals = isBlue ? match.getBlueScore() : match.getRedScore();
        stats.setGoals(stats.getGoals() - goals);

        stats.setLongestWinStreak(getLongestStreakByPlayerId(player.getId()));
        stats.setCurrentWinStreak(getLatestStreakByPlayerId(player.getId()));

        stats.setHighestELO(Math.max(highestELO, 1200));
        stats.setLowestELO(Math.min(lowestELO,1200));

        soloPlayerStatsRepository.save(stats);
    }

    public int getLongestStreakByPlayerId(Long playerId){
        return soloStreakRepository.findTopByPlayerIdOrderByWinStreakDesc(playerId)
                .map(SoloStreak::getWinStreak)
                .orElse(0);
    }

    public int getLatestStreakByPlayerId(Long playerId){
        return soloStreakRepository.findTopByPlayerIdOrderByMatchIdDesc(playerId)
                .map(SoloStreak::getWinStreak)
                .orElse(0);
    }

    public void deleteStreakByMatchId(Long matchId){
        soloStreakRepository.deleteAllByMatchId(matchId);
    }

}
