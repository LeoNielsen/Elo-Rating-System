package EloRatingSystem.Modules.Rating.Services;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Rating.Dtos.TeamRatingResponseDto;
import EloRatingSystem.Modules.Rating.Models.TeamRating;
import EloRatingSystem.Modules.Rating.Repositories.TeamRatingRepository;
import EloRatingSystem.Modules.Stats.Models.DailyStats.TeamDailyStats;
import EloRatingSystem.Modules.Stats.Repositories.Daily.TeamDailyStatsRepository;
import EloRatingSystem.Modules.Stats.Services.TeamStatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import EloRatingSystem.Modules.Team.Repositories.TeamPairRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TeamRatingService {


    @Autowired
    TeamRatingRepository teamRatingRepository;
    @Autowired
    TeamPairRepository teamPairRepository;
    @Autowired
    TeamDailyStatsRepository teamDailyStatsRepository;
    @Autowired
    TeamStatsService teamStatsService;
    @Autowired
    RatingUtils ratingUtils;


    public Mono<List<TeamRatingResponseDto>> getTeamRatingByMatchId(Long id) {
        List<TeamRating> ratings = teamRatingRepository.findAllByMatchId(id);
        List<TeamRatingResponseDto> dtoList = ratings.stream()
                .map(TeamRatingResponseDto::new)
                .toList();
        return Mono.just(dtoList);
    }

//    public Mono<List<ChartDataDto>> getSoloChartData() {
//        List<TeamDailyStats> dailyStatsList = teamDailyStatsRepository.findAll();
//        List<ChartDataDto> chartDataDtoList = dailyStatsList.stream()
//                .map(ChartDataDto::new)
//                .toList();
//        return Mono.just(chartDataDtoList);
//    }

    public void newTeamRating(Match match) {
        boolean redWon = ratingUtils.isWinner(match.getRedTeamScore(), match.getBlueTeamScore());
        Team winner = redWon ? match.getRedTeam() : match.getBlueTeam();
        Team loser = redWon ? match.getBlueTeam() : match.getRedTeam();

        teamRankingCalculator(winner, loser, match);
    }

    private void teamRankingCalculator(Team winner, Team loser, Match match) {
        double pointMultiplier = ratingUtils.calculatePointMultiplier(match.getRedTeamScore(), match.getBlueTeamScore());

        TeamPair winnerPair = winner.getPair();
        TeamPair loserPair = loser.getPair();

        double winnerOdds = ratingUtils.calculateOdds(winnerPair.getRating(), loserPair.getRating(), 400);
        double loserOdds = ratingUtils.calculateOdds(loserPair.getRating(), winnerPair.getRating(), 400);

        newTeamRating(winnerPair, pointMultiplier, winnerOdds, true, match);
        newTeamRating(loserPair, pointMultiplier, loserOdds, false, match);
    }

    private void newTeamRating(TeamPair team, double pointMultiplier, double playerOdds, boolean isWinner, Match match) {
        int newTeamRating = ratingUtils.calculateNewRating(team.getRating(), pointMultiplier, playerOdds, isWinner);
        TeamRating teamRating = new TeamRating(match, team, team.getRating(), newTeamRating);
        teamRatingRepository.save(teamRating);
        teamStatsService.updatePlayerStats(team, teamRating);
        updatePlayerDailyStats(LocalDate.now(), newTeamRating - team.getRating(), team, newTeamRating);
        team.setRating(newTeamRating);
        teamPairRepository.save(team);
    }

    public void updatePlayerDailyStats(LocalDate date, int ratingChange, TeamPair teamPair, int playerRating) {
        teamDailyStatsRepository.findAllByTeamPairIdAndDate(teamPair.getId(), date).
                ifPresentOrElse(
                        stats -> {
                            stats.setRatingChange(stats.getRatingChange() + ratingChange);
                            stats.setRating(playerRating);
                            teamDailyStatsRepository.save(stats);
                        },
                        () -> teamDailyStatsRepository.save(new TeamDailyStats(teamPair, date, ratingChange, playerRating))
                );
    }


    public void deleteRatingsByMatch(LocalDate date,Long id) {
        List<TeamRating> playerRatingList = teamRatingRepository.findAllByMatchId(id);
        for (TeamRating rating : playerRatingList) {
            TeamPair team = rating.getTeamPair();
            team.setRating(rating.getOldRating());
            updatePlayerDailyStats(date,rating.getOldRating() - rating.getNewRating(), team, rating.getOldRating());
            teamPairRepository.save(team);
            teamRatingRepository.deleteById(rating.getId());
        }
    }

    public int getHighestELOByTeamPairId(Long teamPairId) {
        return teamRatingRepository.findTopByTeamPairIdOrderByNewRatingDesc(teamPairId)
                .map(TeamRating::getNewRating)
                .orElse(1200);
    }

    public int getLowestELOByTeamPairId(Long teamPairId) {
        return teamRatingRepository.findTopByTeamPairIdOrderByNewRatingAsc(teamPairId)
                .map(TeamRating::getNewRating)
                .orElse(1200);
    }
}
