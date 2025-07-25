package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.PlayerDtos.ChartDataDto;
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
import EloRatingSystem.Services.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SoloRatingService {
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
    @Autowired
    AchievementService achievementService;

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
        updatePlayerStats(player, soloPlayerRating);
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

    public void updatePlayerStats(Player player, SoloPlayerRating rating) {
        SoloMatch match = rating.getSoloMatch();
        boolean isBlue = match.getBluePlayer() == player;
        boolean isBlueWinner = ratingUtils.isWinner(match.getBlueScore(),match.getRedScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;

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
                if (ratingUtils.tenZeroMatch(match.getBlueScore(), match.getRedScore())) {
                    stats.setShutouts(stats.getShutouts() + 1);
                }
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
        achievementService.checkAndUnlockAchievementsSolo(player,match);
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
}
