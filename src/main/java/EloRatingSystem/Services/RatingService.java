package EloRatingSystem.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RatingService {
    public Match newRating(Match match) {
        Team winner;
        Team loser;

        double pointMultiplier = pointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        if (match.getRedTeamScore() > match.getBlueTeamScore()) {
            winner = match.getRedTeam();
            loser = match.getBlueTeam();

            rankingCalculator(winner, loser, pointMultiplier);

            match.setRedTeam(winner);
            match.setBlueTeam(loser);

            return match;
        } else if (match.getBlueTeamScore() > match.getRedTeamScore()) {
            winner = match.getBlueTeam();
            loser = match.getRedTeam();

            rankingCalculator(winner, loser, pointMultiplier);

            match.setBlueTeam(winner);
            match.setRedTeam(loser);

            return match;
        } else {
            Mono.error(new ApiException("Match cannot be a draw", HttpStatus.BAD_REQUEST));
        }
        return match;
    }

    private void rankingCalculator(Team winner, Team loser, double pointMultiplier) {
        double winnerOddsAttacker = playerOdds(winner.getAttacker(), loser);
        double winnerOddsDefender = playerOdds(winner.getDefender(), loser);
        double loserOddsAttacker = playerOdds(loser.getAttacker(), winner);
        double loserOddsDefender = playerOdds(loser.getDefender(), winner);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        winner.setAttacker(newPlayerRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true));
        winner.setDefender(newPlayerRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true));
        loser.setAttacker(newPlayerRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false));
        loser.setDefender(newPlayerRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false));

        winner.setWon(winner.getWon() + 1);
        loser.setLost(loser.getLost() + 1);
    }

    private Player newPlayerRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner) {
        int newPlayerRating = (int) (player.getRating() + ((24 * pointMultiplier) * ((isWinner ? 1.0 : 0.0) - ((teamRating + playerOdds) / 2))));
        player.setRating(newPlayerRating);
        return player;
    }

    public double playerOdds(Player player, Team opponentTeam) {
        double oddsAgainstAttacker = playerOdds(player, opponentTeam.getAttacker());
        log.info("playerOdds " + oddsAgainstAttacker);
        double oddsAgainstDefender = playerOdds(player, opponentTeam.getDefender());
        log.info("playerOdds " + oddsAgainstDefender);

        return (oddsAgainstAttacker + oddsAgainstDefender) / 2;
    }

    private double playerOdds(Player player, Player opponent) {

        return 1 / (1 + (Math.pow(10, (double) (opponent.getRating() - player.getRating()) / 500)));
    }

    private double pointMultiplier(int redTeamScore, int blueTeamScore) {
        return 1 + (Math.pow(Math.log10((Math.abs(redTeamScore - blueTeamScore))), 3));
    }
}
