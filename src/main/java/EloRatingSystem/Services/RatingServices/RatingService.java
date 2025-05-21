package EloRatingSystem.Services.RatingServices;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.PlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.PlayerStatsRepository;
import EloRatingSystem.Reporitories.RatingRepository;
import EloRatingSystem.Services.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RatingService {
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PlayerDailyStatsRepository dailyStatsRepository;
    @Autowired
    PlayerStatsRepository statsRepository;
    @Autowired
    MatchRepository matchRepository;
    @Autowired
    RatingUtils ratingUtils;
    @Autowired
    AchievementService achievementService;

    public Mono<List<RatingResponseDto>> getRatingByMatchId(Long id) {
        List<PlayerRating> ratings = ratingRepository.findAllByMatchId(id);
        List<RatingResponseDto> dtoList = ratings.stream()
                .map(RatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

    public Mono<List<ChartDataDto>> getChartData() {
        List<PlayerRating> ratings = ratingRepository.findAll();
        List<ChartDataDto> chartDataDtoList = ratings.stream()
                .map(rating -> {
                    Match match = matchRepository.findById(rating.getMatch().getId()).orElseThrow();
                    return new ChartDataDto(match.getId(), new PlayerResponseDto(rating.getPlayer()), rating.getNewRating(), match.getDate());
                })
                .toList();
        return Mono.just(chartDataDtoList);
    }

    public Match newRating(Match match) {
        boolean redWon = ratingUtils.isWinner(match.getRedTeamScore(), match.getBlueTeamScore());
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        rankingCalculator(winner, loser, match);

        match.setRedTeam(winner.getId().equals(match.getRedTeam().getId()) ? winner : loser);
        match.setBlueTeam(winner.getId().equals(match.getBlueTeam().getId()) ? winner : loser);

        return match;
    }

    private void rankingCalculator(Team winner, Team loser, Match match) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        double winnerOddsAttacker = ratingUtils.calculatePlayerOdds(winner.getAttacker(), loser);
        double winnerOddsDefender = ratingUtils.calculatePlayerOdds(winner.getDefender(), loser);
        double loserOddsAttacker = ratingUtils.calculatePlayerOdds(loser.getAttacker(), winner);
        double loserOddsDefender = ratingUtils.calculatePlayerOdds(loser.getDefender(), winner);

        double winnerTeamOdds = (winnerOddsAttacker + winnerOddsDefender) / 2;
        double loserTeamOdds = (loserOddsAttacker + loserOddsDefender) / 2;

        winner.setAttacker(newPlayerRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match));
        winner.setDefender(newPlayerRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match));
        loser.setAttacker(newPlayerRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match));
        loser.setDefender(newPlayerRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match));

        winner.setWon(winner.getWon() + 1);
        loser.setLost(loser.getLost() + 1);
    }

    private Player newPlayerRating(Player player, double teamOdds, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        int newPlayerRating = ratingUtils.calculateNewRating(player.getRating(), pointMultiplier, (teamOdds + playerOdds) / 2, isWinner);
        PlayerRating playerRating = new PlayerRating(match, player, player.getRating(), newPlayerRating);
        ratingRepository.save(playerRating);
        updatePlayerStats(player, playerRating);
        updatePlayerDailyStats(newPlayerRating - player.getRating(), player);
        player.setRating(newPlayerRating);
        return player;
    }

    public void updatePlayerDailyStats(int ratingChange, Player player) {
        Date today = new Date(System.currentTimeMillis());
        dailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), today)
                .ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            dailyStatsRepository.save(stats);
                        },
                        () -> dailyStatsRepository.save(new PlayerDailyStats(player, today, ratingChange))
                );
    }

    public void updatePlayerStats(Player player, PlayerRating rating) {
        Match match = rating.getMatch();
        boolean isBlue = ratingUtils.isPlayerInTeam(match.getBlueTeam(),player);
        boolean isBlueWinner = ratingUtils.isWinner(match.getBlueTeamScore(),match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;
        boolean isAttacker = ratingUtils.isAttacker(match.getBlueTeam(),match.getRedTeam(),player);

        Optional<PlayerStats> playerStatsOptional = statsRepository.findByPlayerId(player.getId());
        PlayerStats stats = playerStatsOptional.orElseGet(() ->
                new PlayerStats(
                        player,
                        isAttacker && won ? 1 : 0,
                        !isAttacker && won ? 1 : 0,
                        isAttacker && !won ? 1 : 0,
                        !isAttacker && !won ? 1 : 0,
                        isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                        rating.getNewRating() > rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        rating.getNewRating() < rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0
                )
        );

        if (playerStatsOptional.isPresent()) {
            if (won) {
                if (isAttacker) {
                    stats.setAttackerWins(stats.getAttackerWins() + 1);
                } else {
                    stats.setDefenderWins(stats.getDefenderWins() + 1);
                }
                stats.setCurrentWinStreak(stats.getCurrentWinStreak() + 1);
                stats.setLongestWinStreak(Math.max(stats.getLongestWinStreak(), stats.getCurrentWinStreak()));
            } else {
                if (isAttacker) {
                    stats.setAttackerLost(stats.getAttackerLost() + 1);
                } else {
                    stats.setDefenderLost(stats.getDefenderLost() + 1);
                }
                stats.setCurrentWinStreak(0);
            }

            int newRating = rating.getNewRating();
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }
        statsRepository.save(stats);
        achievementService.checkAndUnlockAchievements(player,match);
    }

    public void deleteRatingsByMatch(Long Id) {
        List<PlayerRating> playerRatingList = ratingRepository.findAllByMatchId(Id);
        for (PlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setRating(rating.getOldRating());
            updatePlayerDailyStats(rating.getOldRating() - rating.getNewRating(), player);
            playerRepository.save(player);
            ratingRepository.deleteById(rating.getId());
        }
    }

}
