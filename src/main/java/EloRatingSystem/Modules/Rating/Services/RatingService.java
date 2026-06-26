package EloRatingSystem.Modules.Rating.Services;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Rating.Dtos.RatingResponseDto;
import EloRatingSystem.Modules.Rating.Models.PlayerRating;
import EloRatingSystem.Modules.Rating.Repositories.RatingRepository;
import EloRatingSystem.Modules.Stats.Dtos.ChartDataDto;
import EloRatingSystem.Modules.Stats.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Modules.Stats.Repositories.PlayerDailyStatsRepository;
import EloRatingSystem.Modules.Stats.Services.StatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    StatsService statsService;
    @Autowired
    RatingUtils ratingUtils;


    public Mono<List<RatingResponseDto>> getRatingByMatchId(Long id) {
        List<PlayerRating> ratings = ratingRepository.findAllByMatchId(id);
        List<RatingResponseDto> dtoList = ratings.stream()
                .map(RatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

    public Mono<List<ChartDataDto>> getChartData() {
        List<PlayerDailyStats> dailyStatsList = dailyStatsRepository.findAll();
        List<ChartDataDto> chartDataDtoList = dailyStatsList.stream()
                .map(ChartDataDto::new)
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

        List<PlayerRating> playerRatingList = new ArrayList<>();

        PlayerRating winnerAtk = newPlayerRating(winner.getAttacker(), winnerTeamOdds, pointMultiplier, winnerOddsAttacker, true, match);
        playerRatingList.add(winnerAtk);
        winner.setAttacker(winnerAtk.getPlayer());

        PlayerRating winnerDef = newPlayerRating(winner.getDefender(), winnerTeamOdds, pointMultiplier, winnerOddsDefender, true, match);
        playerRatingList.add(winnerDef);
        winner.setDefender(winnerDef.getPlayer());

        PlayerRating loserAtk = newPlayerRating(loser.getAttacker(), loserTeamOdds, pointMultiplier, loserOddsAttacker, false, match);
        playerRatingList.add(loserAtk);
        loser.setAttacker(loserAtk.getPlayer());

        PlayerRating loserDef = newPlayerRating(loser.getDefender(), loserTeamOdds, pointMultiplier, loserOddsDefender, false, match);
        playerRatingList.add(loserDef);
        loser.setDefender(loserDef.getPlayer());

        winner.setWon(winner.getWon() + 1);
        loser.setLost(loser.getLost() + 1);

        ratingRepository.saveAll(playerRatingList);
    }

    private PlayerRating newPlayerRating(Player player, double teamOdds, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        int newPlayerRating = ratingUtils.calculateNewRating(player.getRating(), pointMultiplier, (teamOdds + playerOdds) / 2, isWinner);
        PlayerRating playerRating = new PlayerRating(match, player, player.getRating(), newPlayerRating);
        statsService.updatePlayerStats(player, playerRating);
        updatePlayerDailyStats(LocalDate.now(), newPlayerRating - player.getRating(), player, newPlayerRating);
        player.setRating(newPlayerRating);
        return playerRating;
    }

    public void updatePlayerDailyStats(LocalDate date, int ratingChange, Player player, int playerRating) {
        dailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date)
                .ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            stats.setRating(playerRating);
                            dailyStatsRepository.save(stats);
                        },
                        () -> dailyStatsRepository.save(new PlayerDailyStats(player, date, ratingChange, playerRating))
                );
    }

    public void deleteRatingsByMatch(LocalDate date, Long Id) {
        List<PlayerRating> playerRatingList = ratingRepository.findAllByMatchId(Id);
        for (PlayerRating rating : playerRatingList) {
            Player player = rating.getPlayer();
            player.setRating(rating.getOldRating());
            updatePlayerDailyStats(date, rating.getOldRating() - rating.getNewRating(), player, rating.getOldRating());
            playerRepository.save(player);
            ratingRepository.deleteById(rating.getId());
        }
    }

    public int getHighestELOByPlayerId(Long playerId) {
        return ratingRepository.findTopByPlayerIdOrderByNewRatingDesc(playerId)
                .map(PlayerRating::getNewRating)
                .orElse(1200);
    }

    public int getLowestELOByPlayerId(Long playerId) {
        return ratingRepository.findTopByPlayerIdOrderByNewRatingAsc(playerId)
                .map(PlayerRating::getNewRating)
                .orElse(1200);
    }

}
