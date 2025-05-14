package EloRatingSystem.Services;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @Autowired
    SoloPlayerDailyStatsRepository soloPlayerDailyStatsRepository;
    @Autowired
    SoloPlayerStatsRepository soloPlayerStatsRepository;

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
        SoloPlayerRating soloPlayerRating = new SoloPlayerRating(match, player, player.getSoloRating(), newPlayerRating);
        soloRatingRepository.save(soloPlayerRating);
        updatePlayerStats(player,soloPlayerRating);
        updatePlayerDailyStats(newPlayerRating - player.getSoloRating(), player);
        player.setSoloRating(newPlayerRating);
    }

    private void updatePlayerStats(Player player, SoloPlayerRating playerRating) {
        SoloMatch match = playerRating.getSoloMatch();
        boolean isBlue = match.getBluePlayer() == player;
        boolean won = isBlue && match.getBlueScore() > match.getRedScore() || !isBlue && match.getRedScore() > match.getBlueScore();
        SoloPlayerStats stats;
        Optional<SoloPlayerStats> playerStatsOptional = soloPlayerStatsRepository.findByPlayerId(player.getId());
        if (playerStatsOptional.isPresent()) {
            stats = playerStatsOptional.get();
            if (won) {
                stats.setWins(stats.getWins() + 1);
                stats.setCurrentWinStreak(stats.getCurrentWinStreak() + 1);
                if (stats.getCurrentWinStreak() > stats.getLongestWinStreak()) {
                    stats.setLongestWinStreak(stats.getCurrentWinStreak());
                }
            } else {
                stats.setLost(stats.getLost() + 1);
                stats.setCurrentWinStreak(0);
            }

            int currentElo = player.getRating();
            if (currentElo > stats.getHighestELO()) {
                stats.setHighestELO(currentElo);
            }
            if (currentElo < stats.getLowestELO()) {
                stats.setLowestELO(currentElo);
            }

            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueScore() : match.getRedScore()));
        } else {
            stats = new SoloPlayerStats(
                    player,
                    won ? 1 : 0,
                    !won ? 1 : 0,
                    isBlue ? match.getBlueScore() : match.getRedScore(),
                    playerRating.getNewRating(),
                    playerRating.getOldRating(),
                    won ? 1 : 0,
                    won ? 1 : 0
            );
        }
        soloPlayerStatsRepository.save(stats);
    }

    private void updatePlayerDailyStats(int change, Player player) {
        Date date = new Date(System.currentTimeMillis());
        Optional<SoloPlayerDailyStats> playerDailyStatsOptional = soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date);
        if (playerDailyStatsOptional.isPresent()) {
            SoloPlayerDailyStats playerDailyStats = playerDailyStatsOptional.get();
            playerDailyStats.setRatingChange(playerDailyStats.getRatingChange() + change);
            soloPlayerDailyStatsRepository.save(playerDailyStats);
        } else {
            soloPlayerDailyStatsRepository.save(new SoloPlayerDailyStats(player, date, change));
        }
    }

    public void deleteRatingsBySoloMatch(Long id) {
        List<SoloPlayerRating> playerRatingList = soloRatingRepository.findAllBySoloMatchId(id);
        for (SoloPlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setSoloRating(rating.getOldRating());
            updatePlayerDailyStats(rating.getOldRating() - rating.getNewRating(), player);
            playerRepository.save(player);
            soloRatingRepository.deleteById(rating.getId());
        }
    }
}
