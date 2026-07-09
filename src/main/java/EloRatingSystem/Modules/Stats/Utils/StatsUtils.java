package EloRatingSystem.Modules.Stats.Utils;

import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.stereotype.Service;

@Service
public class StatsUtils {

    public boolean tenZeroMatch(int score1, int score2) {
        return score1 == 0 || score2 == 0;
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
