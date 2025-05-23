package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.Team;
import org.springframework.stereotype.Service;

@Service
public class RatingUtils {

    public boolean tenZeroMatch(int score1, int score2) {
        return score1 == 0 || score2 == 0;
    }

    public double calculatePointMultiplier(int score1, int score2) {
        return 1 + Math.pow(Math.log10(Math.abs(score1 - score2)), 3);
    }

    public double calculatePlayerOdds(Player player, Team opponentTeam) {
        double oddsAgainstAttacker = calculateOdds(player.getRating(), opponentTeam.getAttacker().getRating(), 500);
        double oddsAgainstDefender = calculateOdds(player.getRating(), opponentTeam.getDefender().getRating(), 500);
        return (oddsAgainstAttacker + oddsAgainstDefender) / 2;
    }

    public double calculateOdds(int playerRating, int opponentRating, int divisor) {
        return 1.0 / (1.0 + Math.pow(10, (double) (opponentRating - playerRating) / divisor));
    }

    public int calculateNewRating(int currentRating, double multiplier, double odds, boolean isWinner) {
        return (int) Math.round(currentRating + (32 * multiplier) * ((isWinner ? 1.0 : 0.0) - odds));
    }

    public boolean isWinner(int playerScore, int opponentScore) {
        return playerScore > opponentScore;
    }

    public boolean isPlayerInTeam(Team team, Player player) {
        return team.getAttacker().equals(player) || team.getDefender().equals(player);
    }

    public boolean isAttacker(Team team, Team team2, Player player) {
        return team.getAttacker().equals(player) || team2.getAttacker().equals(player);
    }

}
