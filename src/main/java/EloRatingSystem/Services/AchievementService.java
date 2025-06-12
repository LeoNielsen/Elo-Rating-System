package EloRatingSystem.Services;

import EloRatingSystem.Models.*;
import EloRatingSystem.Models.Achievement.Achievement;
import EloRatingSystem.Models.Achievement.GameType;
import EloRatingSystem.Models.Achievement.PlayerAchievement;
import EloRatingSystem.Reporitories.Achievements.AchievementRepository;
import EloRatingSystem.Reporitories.Achievements.PlayerAchievementRepository;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyWinnerRepository;
import EloRatingSystem.Reporitories.PlayerStatsRepository;
import EloRatingSystem.Reporitories.SoloPlayerStatsRepository;
import EloRatingSystem.Services.RatingServices.RatingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AchievementService {

    @Autowired
    PlayerStatsRepository statsRepository;
    @Autowired
    SoloPlayerStatsRepository soloStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    AchievementRepository achievementRepository;
    @Autowired
    PlayerAchievementRepository playerAchievementRepository;
    @Autowired
    RatingUtils ratingUtils;
    @Autowired
    MonthlyWinnerRepository monthlyWinnerRepository;

    public void checkAndUnlockAchievements(Player player, Match match) {
        PlayerStats playerStats = statsRepository.findByPlayerId(player.getId())
                .orElseGet(() -> new PlayerStats(player));

        int rating = playerStats.getHighestELO();
        int currentRating = player.getRating();
        int totalWins = playerStats.getAttackerWins() + playerStats.getDefenderWins();
        int winStreak = playerStats.getLongestWinStreak();
        boolean winTenZeroAsDef = false;
        boolean winTenZeroAsAtk = false;

        boolean isBlue = ratingUtils.isPlayerInTeam(match.getBlueTeam(), player);
        boolean isBlueWinner = ratingUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = ratingUtils.isAttacker(match.getBlueTeam(), match.getRedTeam(), player);


        if (won) {
            if (match.getRedTeamScore() == 0 || match.getBlueTeamScore() == 0) {
                if (isAttacker) {
                    winTenZeroAsAtk = true;
                } else {
                    winTenZeroAsDef = true;
                }
            }
        }

        List<Achievement> allAchievements = achievementRepository.findAllByGameType(GameType.TEAMS);
        for (Achievement achievement : allAchievements) {
            boolean qualifies = switch (achievement.getType()) {
                case RATING_REACHED -> rating >= achievement.getAmount();
                case TOTAL_WINS -> totalWins >= achievement.getAmount();
                case WIN_STREAK -> winStreak >= achievement.getAmount();
                case WIN_10_ZERO_AS_DEFENDER -> winTenZeroAsDef;
                case WIN_10_ZERO_AS_ATTACKER -> winTenZeroAsAtk;
                case WIN_AGAINST_HIGHER_RATED -> won && lowerRatingThenTeam(currentRating,
                        isBlue ? match.getRedTeam() : match.getBlueTeam(), achievement.getAmount());
                default -> false;
            };

            if (qualifies && !playerAchievementIsUnlocked(player, achievement)) {
                unlockAchievement(player, achievement);
            }
        }
    }

    public void checkAndUnlockAchievementsSolo(Player player, SoloMatch match) {
        SoloPlayerStats playerStats = soloStatsRepository.findByPlayerId(player.getId())
                .orElseGet(() -> new SoloPlayerStats(player));

        int rating = playerStats.getHighestELO();
        int currentRating = player.getSoloRating();
        int totalWins = playerStats.getWins();
        int winStreak = playerStats.getLongestWinStreak();
        boolean winTenZero = false;

        boolean isBlue = match.getBluePlayer() == player;
        boolean isBlueWinner = ratingUtils.isWinner(match.getBlueScore(),match.getRedScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;


        if (won) {
            if (match.getRedScore() == 0 || match.getBlueScore() == 0) {
                winTenZero = true;
            }
        }

        List<Achievement> allAchievements = achievementRepository.findAllByGameType(GameType.SOLO);
        for (Achievement achievement : allAchievements) {
            boolean qualifies = switch (achievement.getType()) {
                case RATING_REACHED_SOLO -> rating >= achievement.getAmount();
                case TOTAL_WINS_SOLO -> totalWins >= achievement.getAmount();
                case WIN_STREAK_SOLO -> winStreak >= achievement.getAmount();
                case WIN_10_ZERO_SOLO -> winTenZero;
                case WIN_AGAINST_HIGHER_RATED_SOLO -> won && lowerRatingThenPlayer(currentRating,
                        isBlue? match.getRedPlayer().getRating() : match.getBluePlayer().getRating(),
                        achievement.getAmount());
                default -> false;
            };

            if (qualifies && !playerAchievementIsUnlocked(player, achievement)) {
                unlockAchievement(player, achievement);
            }
        }
    }

    public void checkAndUnlockAchievementsMonthly(Player player) {
        List<MonthlyWinner> monthlyWins = monthlyWinnerRepository.findAllByPlayerId(player.getId());

        List<Achievement> allAchievements = achievementRepository.findAllByGameType(GameType.MONTHLY);
        for (Achievement achievement : allAchievements) {
            boolean qualifies = switch (achievement.getType()) {
                case TOTAL_MONTHLY_2V2_WINS -> monthlyWins.size() >= achievement.getAmount();
                default -> false;
            };

            if (qualifies && !playerAchievementIsUnlocked(player, achievement)) {
                unlockAchievement(player, achievement);
            }
        }
    }

    private void unlockAchievement(Player player, Achievement achievement) {
        Optional<PlayerAchievement> playerAchievementOptional = playerAchievementRepository
                .findByPlayerIdAndAchievementId(player.getId(), achievement.getId());
        if (playerAchievementOptional.isPresent()) {
            PlayerAchievement playerAchievement = playerAchievementOptional.get();
            playerAchievement.setUnlocked(true);
            playerAchievementRepository.save(playerAchievement);
        } else {
            playerAchievementRepository.save(new PlayerAchievement(player,achievement,true));
        }
    }

    private boolean playerAchievementIsUnlocked(Player player, Achievement achievement) {
        return playerAchievementRepository.findByPlayerIdAndAchievementId(player.getId(), achievement.getId())
                .map(PlayerAchievement::isUnlocked).orElse(false);
    }

    private boolean lowerRatingThenTeam(int rating, Team opponentTeam, int compareRating) {
        return lowerRatingThenPlayer(rating, opponentTeam.getAttacker().getRating(), compareRating) ||
                lowerRatingThenPlayer(rating, opponentTeam.getDefender().getRating(), compareRating);
    }

    private boolean lowerRatingThenPlayer(int rating, int opponentRating, int compareRating) {
        return opponentRating - rating >= compareRating;
    }


}
