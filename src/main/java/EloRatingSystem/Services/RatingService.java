package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.RatingRepository;
import EloRatingSystem.Reporitories.SoloRatingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RatingService {
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    SoloRatingRepository soloRatingRepository;

    @Autowired
    PlayerRepository playerRepository;

    public Mono<List<RatingResponseDto>> getRatingByMatchId(Long id) {
        List<PlayerRating> ratings = ratingRepository.findAllByMatchId(id);

        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (PlayerRating rating : ratings) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getMatch().getId(), new PlayerResponseDto(rating.getPlayer()), rating.getOldRating(), rating.getNewRating()));
        }

        return Mono.just(ratingResponseDtoList);
    }

    public Match newRating(Match match) {
        Team winner;
        Team loser;

        double pointMultiplier = pointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        if (match.getRedTeamScore() > match.getBlueTeamScore()) {
            winner = match.getRedTeam();
            loser = match.getBlueTeam();

            rankingCalculator(winner, loser, pointMultiplier, match);

            match.setRedTeam(winner);
            match.setBlueTeam(loser);

        } else if (match.getBlueTeamScore() > match.getRedTeamScore()) {
            winner = match.getBlueTeam();
            loser = match.getRedTeam();

            rankingCalculator(winner, loser, pointMultiplier, match);

            match.setBlueTeam(winner);
            match.setRedTeam(loser);

        } else {
            Mono.error(new ApiException("Match cannot be a draw", HttpStatus.BAD_REQUEST));
        }
        return match;
    }

    private void rankingCalculator(Team winner, Team loser, double pointMultiplier, Match match) {
        double winnerOddsAttacker = playerOddsTeam(winner.getAttacker(), loser);
        double winnerOddsDefender = playerOddsTeam(winner.getDefender(), loser);
        double loserOddsAttacker = playerOddsTeam(loser.getAttacker(), winner);
        double loserOddsDefender = playerOddsTeam(loser.getDefender(), winner);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        winner.setAttacker(newPlayerRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match));
        winner.setDefender(newPlayerRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match));
        loser.setAttacker(newPlayerRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match));
        loser.setDefender(newPlayerRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match));

        winner.setWon(winner.getWon() + 1);
        loser.setLost(loser.getLost() + 1);
    }

    private Player newPlayerRating(Player player, double teamRating, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        int newPlayerRating = (int) Math.round(player.getRating() + ((32 * pointMultiplier) * ((isWinner ? 1.0 : 0.0) - ((teamRating + playerOdds) / 2))));
        ratingRepository.save(new PlayerRating(match, player, player.getRating(), newPlayerRating));
        player.setRating(newPlayerRating);
        return player;
    }

    public double playerOddsTeam(Player player, Team opponentTeam) {
        double oddsAgainstAttacker = playerOdds(player, opponentTeam.getAttacker());
        double oddsAgainstDefender = playerOdds(player, opponentTeam.getDefender());

        return (oddsAgainstAttacker + oddsAgainstDefender) / 2;
    }

    private double playerOdds(Player player, Player opponent) {
        return 1 / (1 + (Math.pow(10, (double) (opponent.getRating() - player.getRating()) / 500)));
    }

    private double pointMultiplier(int redTeamScore, int blueTeamScore) {
        return 1 + (Math.pow(Math.log10((Math.abs(redTeamScore - blueTeamScore))), 3));
    }

    public void deleteRatingsByMatch(Long Id) {
        List<PlayerRating> playerRatingList = ratingRepository.findAllByMatchId(Id);
        for (PlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setRating(rating.getOldRating());
            playerRepository.save(player);
            ratingRepository.deleteById(rating.getId());
        }
    }

    public Mono<List<RatingResponseDto>> getAllRatings() {
        List<PlayerRating> ratingList = ratingRepository.findAll();
        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (PlayerRating rating : ratingList) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getMatch().getId(),new PlayerResponseDto(rating.getPlayer()),rating.getOldRating(),rating.getNewRating()));
        }

        return Mono.just(ratingResponseDtoList);
    }

    public SoloMatch newSoloRating(SoloMatch match) {
        Player winner = match.getBlueScore() < match.getRedScore() ? match.getRedPlayer() : match.getBluePlayer();
        Player loser = match.getBlueScore() < match.getRedScore() ? match.getBluePlayer() : match.getRedPlayer();

        double pointMultiplier = pointMultiplier(match.getRedScore(), match.getBlueScore());

        soloRankingCalculator(winner, loser, pointMultiplier, match);

        return match;
    }

    private void soloRankingCalculator(Player winner, Player loser, double pointMultiplier, SoloMatch match) {
        double winnerOdds = playerOddsSolo(winner, loser);
        double loserOdds = playerOddsSolo(loser, winner);

        newPlayerSoloRating(winner, pointMultiplier, winnerOdds, true, match);
        newPlayerSoloRating(loser, pointMultiplier, loserOdds, false, match);

        // TODO add wins and lost
    }

    private double playerOddsSolo(Player player, Player opponent) {
        return 1 / (1 + (Math.pow(10, (double) (opponent.getSoloRating() - player.getSoloRating()) / 400)));
    }

    private void newPlayerSoloRating(Player player, double pointMultiplier, double playerOdds, boolean isWinner, SoloMatch match) {
        int newPlayerRating = (int) Math.round(player.getSoloRating() + ((32 * pointMultiplier) * ((isWinner ? 1.0 : 0.0) - (playerOdds))));
        soloRatingRepository.save(new SoloPlayerRating(match, player, player.getSoloRating(), newPlayerRating));
        player.setSoloRating(newPlayerRating);
    }

    public Mono<List<RatingResponseDto>> getSoloRatingBySoloMatchId(Long id) {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAllBySoloMatchId(id);

        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (SoloPlayerRating rating : ratings) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getSoloMatch().getId(), new PlayerResponseDto(rating.getPlayer()), rating.getOldRating(), rating.getNewRating()));
        }
        return Mono.just(ratingResponseDtoList);
    }

    public Mono<List<RatingResponseDto>> getAllSoloRatings() {
        List<SoloPlayerRating> ratingList = soloRatingRepository.findAll();
        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (SoloPlayerRating rating : ratingList) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getSoloMatch().getId(),new PlayerResponseDto(rating.getPlayer()),rating.getOldRating(),rating.getNewRating()));
        }

        return Mono.just(ratingResponseDtoList);
    }
}
