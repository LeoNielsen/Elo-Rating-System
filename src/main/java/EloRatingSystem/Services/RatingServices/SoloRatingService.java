package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Models.SoloPlayerRating;
import EloRatingSystem.Models.SoloPlayerStats;
import EloRatingSystem.Reporitories.DailyStats.SoloPlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.SoloMatchRepository;
import EloRatingSystem.Reporitories.SoloPlayerStatsRepository;
import EloRatingSystem.Reporitories.SoloRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
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
    @Autowired
    RatingUtils ratingUtils;

    public Mono<List<RatingResponseDto>> getSoloRatingBySoloMatchId(Long id) {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAllBySoloMatchId(id);
        List<RatingResponseDto> dtoList = ratings.stream()
                .map(RatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

    public Mono<List<ChartDataDto>> getSoloChartData() {
        List<SoloPlayerRating> ratings = soloRatingRepository.findAll();
        List<ChartDataDto> chartDataDtoList = ratings.stream()
                .map(rating -> {
                    SoloMatch match = soloMatchRepository.findById(rating.getSoloMatch().getId()).orElseThrow();
                    return new ChartDataDto(match.getId(), new PlayerResponseDto(rating.getPlayer()), rating.getNewRating(), match.getDate());
                })
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
        updatePlayerStats(player, soloPlayerRating);
        updatePlayerDailyStats(newPlayerRating - player.getSoloRating(), player);
        player.setSoloRating(newPlayerRating);
    }

    public void updatePlayerDailyStats(int ratingChange, Player player) {
        Date today = new Date(System.currentTimeMillis());
        soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today).
                ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            soloPlayerDailyStatsRepository.save(stats);
                        },
                        () -> soloPlayerDailyStatsRepository.save(new SoloPlayerDailyStats(player, today, ratingChange))
                );
    }

    private void updatePlayerStats(Player player, SoloPlayerRating playerRating) {
        SoloMatch match = playerRating.getSoloMatch();
        boolean isBlue = match.getBluePlayer() == player;
        boolean won = isBlue && match.getBlueScore() > match.getRedScore() || !isBlue && match.getRedScore() > match.getBlueScore();

        Optional<SoloPlayerStats> playerStatsOptional = soloPlayerStatsRepository.findByPlayerId(player.getId());
        SoloPlayerStats stats = playerStatsOptional.orElseGet(() ->
                new SoloPlayerStats(
                        player,
                        won ? 1 : 0,
                        !won ? 1 : 0,
                        isBlue ? match.getBlueScore() : match.getRedScore(),
                        playerRating.getNewRating(),
                        playerRating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0
                )
        );

        if (playerStatsOptional.isPresent()) {
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
        }

        soloPlayerStatsRepository.save(stats);
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
