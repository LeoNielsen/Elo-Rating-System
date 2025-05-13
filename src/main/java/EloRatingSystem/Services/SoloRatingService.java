package EloRatingSystem.Services;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Models.SoloPlayerRating;
import EloRatingSystem.Reporitories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class SoloRatingService {
    @Autowired
    RatingService ratingService;
    @Autowired
    SoloRatingRepository soloRatingRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    SoloMatchRepository soloMatchRepository;

    public Mono<List<RatingResponseDto>> getSoloRatingBySoloMatchId(Long id) {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAllBySoloMatchId(id);

        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (SoloPlayerRating rating : ratings) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getSoloMatch().getId(), new PlayerResponseDto(rating.getPlayer()), rating.getOldRating(), rating.getNewRating()));
        }
        return Mono.just(ratingResponseDtoList);
    }

    public Mono<List<ChartDataDto>> getSoloChartData() {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAll();
        List<ChartDataDto> chartDataDtoList = new ArrayList<>();
        for (SoloPlayerRating rating : ratings) {
            SoloMatch match = soloMatchRepository.findById(rating.getSoloMatch().getId()).orElseThrow();
            chartDataDtoList.add(new ChartDataDto(match.getId(), new PlayerResponseDto(rating.getPlayer()), rating.getNewRating(), match.getDate()));
        }
        return Mono.just(chartDataDtoList);
    }

    public SoloMatch newSoloRating(SoloMatch match) {
        Player winner = match.getBlueScore() < match.getRedScore() ? match.getRedPlayer() : match.getBluePlayer();
        Player loser = match.getBlueScore() < match.getRedScore() ? match.getBluePlayer() : match.getRedPlayer();

        double pointMultiplier = ratingService.pointMultiplier(match.getRedScore(), match.getBlueScore());

        soloRankingCalculator(winner, loser, pointMultiplier, match);

        return match;
    }

    private void soloRankingCalculator(Player winner, Player loser, double pointMultiplier, SoloMatch match) {
        double winnerOdds = playerOddsSolo(winner, loser);
        double loserOdds = playerOddsSolo(loser, winner);

        newPlayerSoloRating(winner, pointMultiplier, winnerOdds, true, match);
        newPlayerSoloRating(loser, pointMultiplier, loserOdds, false, match);
    }

    private double playerOddsSolo(Player player, Player opponent) {
        return 1 / (1 + (Math.pow(10, (double) (opponent.getSoloRating() - player.getSoloRating()) / 400)));
    }

    private void newPlayerSoloRating(Player player, double pointMultiplier, double playerOdds, boolean isWinner, SoloMatch match) {
        int newPlayerRating = (int) Math.round(player.getSoloRating() + ((32 * pointMultiplier) * ((isWinner ? 1.0 : 0.0) - (playerOdds))));
        soloRatingRepository.save(new SoloPlayerRating(match, player, player.getSoloRating(), newPlayerRating));
        player.setSoloRating(newPlayerRating);
    }
    public Mono<List<RatingResponseDto>> getAllSoloRatings() {
        List<SoloPlayerRating> ratingList = soloRatingRepository.findAll();
        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (SoloPlayerRating rating : ratingList) {
            ratingResponseDtoList.add(new RatingResponseDto(rating.getSoloMatch().getId(), new PlayerResponseDto(rating.getPlayer()), rating.getOldRating(), rating.getNewRating()));
        }

        return Mono.just(ratingResponseDtoList);
    }
    public void deleteRatingsBySoloMatch(Long id) {
        List<SoloPlayerRating> playerRatingList = soloRatingRepository.findAllBySoloMatchId(id);
        for (SoloPlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setSoloRating(rating.getOldRating());
            playerRepository.save(player);
            soloRatingRepository.deleteById(rating.getId());
        }
    }
}
