package EloRatingSystem.Modules.Rating.Services;

import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Rating.Dtos.RatingResponseDto;
import EloRatingSystem.Modules.Rating.Models.SoloPlayerRating;
import EloRatingSystem.Modules.Rating.Repositories.SoloRatingRepository;
import EloRatingSystem.Modules.Stats.Dtos.ChartDataDto;
import EloRatingSystem.Modules.Stats.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Modules.Stats.Repositories.Daily.SoloPlayerDailyStatsRepository;
import EloRatingSystem.Modules.Stats.Services.SoloStatsService;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
public class SoloRatingService {
    @Autowired
    SoloRatingRepository soloRatingRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    SoloPlayerDailyStatsRepository soloPlayerDailyStatsRepository;
    @Autowired
    RatingUtils ratingUtils;
    @Autowired
    SoloStatsService soloStatsService;


    public Mono<List<RatingResponseDto>> getSoloRatingBySoloMatchId(Long id) {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAllBySoloMatchId(id);
        List<RatingResponseDto> dtoList = ratings.stream()
                .map(RatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

    public Mono<List<ChartDataDto>> getSoloChartData() {
        List<SoloPlayerDailyStats> dailyStatsList = soloPlayerDailyStatsRepository.findAll();
        List<ChartDataDto> chartDataDtoList = dailyStatsList.stream()
                .map(ChartDataDto::new)
                .toList();
        return Mono.just(chartDataDtoList);
    }

    public SoloMatch newSoloRating(SoloMatch match) {
        boolean redWon = ratingUtils.isWinner(match.getRedScore(), match.getBlueScore());
        Player winner = redWon ? match.getRedPlayer() : match.getBluePlayer();
        Player loser = redWon ? match.getBluePlayer() : match.getRedPlayer();

        soloRankingCalculator(winner, loser, match);

        return match;
    }

    private void soloRankingCalculator(Player winner, Player loser, SoloMatch match) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedScore(), match.getBlueScore());

        double winnerOdds = ratingUtils.calculateOdds(winner.getSoloRating(), loser.getSoloRating(), 400);
        double loserOdds = ratingUtils.calculateOdds(loser.getSoloRating(), winner.getSoloRating(), 400);

        newPlayerSoloRating(winner, pointMultiplier, winnerOdds, true, match);
        newPlayerSoloRating(loser, pointMultiplier, loserOdds, false, match);
    }

    private void newPlayerSoloRating(Player player, double pointMultiplier, double playerOdds, boolean isWinner, SoloMatch match) {
        int newPlayerRating = ratingUtils.calculateNewRating(player.getSoloRating(), pointMultiplier, playerOdds, isWinner);
        SoloPlayerRating soloPlayerRating = new SoloPlayerRating(match, player, player.getSoloRating(), newPlayerRating);
        soloRatingRepository.save(soloPlayerRating);
        soloStatsService.updatePlayerStats(player, soloPlayerRating);
        updatePlayerDailyStats(LocalDate.now(),newPlayerRating - player.getSoloRating(), player,newPlayerRating);
        player.setSoloRating(newPlayerRating);
    }

    public void updatePlayerDailyStats(LocalDate date,int ratingChange, Player player,int playerRating) {
        soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date).
                ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            stats.setRating(playerRating);
                            soloPlayerDailyStatsRepository.save(stats);
                        },
                        () -> soloPlayerDailyStatsRepository.save(new SoloPlayerDailyStats(player, date, ratingChange, playerRating))
                );
    }



    public void deleteRatingsBySoloMatch(LocalDate date,Long id) {
        List<SoloPlayerRating> playerRatingList = soloRatingRepository.findAllBySoloMatchId(id);
        for (SoloPlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setSoloRating(rating.getOldRating());
            updatePlayerDailyStats(date,rating.getOldRating() - rating.getNewRating(), player, rating.getOldRating());
            playerRepository.save(player);
            soloRatingRepository.deleteById(rating.getId());
        }
    }

    public int getHighestELOByPlayerId(Long playerId) {
        return soloRatingRepository.findTopByPlayerIdOrderByNewRatingDesc(playerId)
                .map(SoloPlayerRating::getNewRating)
                .orElse(1200);
    }

    public int getLowestELOByPlayerId(Long playerId) {
        return soloRatingRepository.findTopByPlayerIdOrderByNewRatingAsc(playerId)
                .map(SoloPlayerRating::getNewRating)
                .orElse(1200);
    }
}
