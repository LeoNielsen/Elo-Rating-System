package EloRatingSystem.Services;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
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
    PlayerDailyStatsRepository playerDailyStatsRepository;
    @Autowired
    PlayerStatsRepository playerStatsRepository;
    @Autowired
    MatchRepository matchRepository;


    public Mono<List<RatingResponseDto>> getAllRatings() {
        List<PlayerRating> ratingList = ratingRepository.findAll();
        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (PlayerRating rating : ratingList) {
            ratingResponseDtoList.add(new RatingResponseDto(rating));
        }

        return Mono.just(ratingResponseDtoList);
    }



    public Mono<List<RatingResponseDto>> getRatingByMatchId(Long id) {
        List<PlayerRating> ratings = ratingRepository.findAllByMatchId(id);

        List<RatingResponseDto> ratingResponseDtoList = new ArrayList<>();
        for (PlayerRating rating : ratings) {
            ratingResponseDtoList.add(new RatingResponseDto(rating));
        }

        return Mono.just(ratingResponseDtoList);
    }

    public Mono<List<ChartDataDto>> getChartData() {
        List<PlayerRating> ratings = ratingRepository.findAll();
        List<ChartDataDto> chartDataDtoList = new ArrayList<>();
        for (PlayerRating rating : ratings) {
            Match match = matchRepository.findById(rating.getMatch().getId()).orElseThrow();
            chartDataDtoList.add(new ChartDataDto(match.getId(), new PlayerResponseDto(rating.getPlayer()), rating.getNewRating(), match.getDate()));
        }
        return Mono.just(chartDataDtoList);
    }

    public Match newRating(Match match) {
        Team winner = match.getRedTeamScore() > match.getBlueTeamScore() ? match.getRedTeam() : match.getBlueTeam();
        Team loser = match.getRedTeamScore() > match.getBlueTeamScore() ? match.getBlueTeam() : match.getRedTeam();

        double pointMultiplier = pointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        rankingCalculator(winner, loser, pointMultiplier, match);

        match.setRedTeam(winner.getId().equals(match.getRedTeam().getId()) ? winner : loser);
        match.setBlueTeam(winner.getId().equals(match.getBlueTeam().getId()) ? winner : loser);

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
        PlayerRating playerRating = new PlayerRating(match, player, player.getRating(), newPlayerRating);
        ratingRepository.save(playerRating);
        updatePlayerStats(player, playerRating);
        updatePlayerDailyStats(newPlayerRating - player.getRating(), player);
        player.setRating(newPlayerRating);
        return player;
    }

    private void updatePlayerStats(Player player, PlayerRating playerRating) {
        Match match = playerRating.getMatch();
        boolean isBlue = (player == match.getBlueTeam().getAttacker() || player == match.getBlueTeam().getDefender());
        boolean won = isBlue && match.getBlueTeamScore() > match.getRedTeamScore() || isBlue && match.getRedTeamScore() > match.getBlueTeamScore();
        boolean isAttacker = (player == match.getBlueTeam().getAttacker() || player == match.getRedTeam().getAttacker());
        PlayerStats stats;
        Optional<PlayerStats> playerStatsOptional = playerStatsRepository.findByPlayerId(player.getId());
        if (playerStatsOptional.isPresent()) {
            stats = playerStatsOptional.get();
            if (won) {
                if (isAttacker) {
                    stats.setAttackerWins(stats.getAttackerWins() + 1);
                } else {
                    stats.setDefenderWins(stats.getDefenderWins() + 1);
                }
                stats.setCurrentWinStreak(stats.getCurrentWinStreak() + 1);
                if (stats.getCurrentWinStreak() > stats.getLongestWinStreak()) {
                    stats.setLongestWinStreak(stats.getCurrentWinStreak());
                }
            } else {
                if (isAttacker) {
                    stats.setAttackerLost(stats.getAttackerLost() + 1);
                } else {
                    stats.setDefenderLost(stats.getDefenderLost() + 1);
                }
                stats.setCurrentWinStreak(0);
            }

            int currentElo = player.getRating();
            if (currentElo > stats.getHighestELO()) {
                stats.setHighestELO(currentElo);
            }
            if (currentElo < stats.getLowestELO()) {
                stats.setLowestELO(currentElo);
            }

            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        } else {

            stats = new PlayerStats(
                    player,
                    isAttacker && won ? 1 : 0,
                    !isAttacker && won ? 1 : 0,
                    isAttacker && !won ? 1 : 0,
                    !isAttacker && !won ? 1 : 0,
                    isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                    playerRating.getNewRating(),
                    playerRating.getOldRating(),
                    won ? 1 : 0,
                    won ? 1 : 0
            );
        }
        playerStatsRepository.save(stats);
    }

    private void updatePlayerDailyStats(int change, Player player) {
        Date date = new Date(System.currentTimeMillis());
        Optional<PlayerDailyStats> playerDailyStatsOptional = playerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date);
        if (playerDailyStatsOptional.isPresent()) {
            PlayerDailyStats playerDailyStats = playerDailyStatsOptional.get();
            playerDailyStats.setRatingChange(playerDailyStats.getRatingChange() + change);
            playerDailyStatsRepository.save(playerDailyStats);
        } else {
            playerDailyStatsRepository.save(new PlayerDailyStats(player, date, change));
        }
    }

    public double playerOddsTeam(Player player, Team opponentTeam) {
        double oddsAgainstAttacker = playerOdds(player, opponentTeam.getAttacker());
        double oddsAgainstDefender = playerOdds(player, opponentTeam.getDefender());

        return (oddsAgainstAttacker + oddsAgainstDefender) / 2;
    }

    private double playerOdds(Player player, Player opponent) {
        return 1 / (1 + (Math.pow(10, (double) (opponent.getRating() - player.getRating()) / 500)));
    }

    protected double pointMultiplier(int redTeamScore, int blueTeamScore) {
        return 1 + (Math.pow(Math.log10((Math.abs(redTeamScore - blueTeamScore))), 3));
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
