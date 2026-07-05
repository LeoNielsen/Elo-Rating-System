package EloRatingSystem.Modules.Stats.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Achievement.Services.AchievementService;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Rating.Models.TeamRating;
import EloRatingSystem.Modules.Stats.Dtos.TeamStatisticsResponseDto;
import EloRatingSystem.Modules.Stats.Models.Streaks.TeamStreak;
import EloRatingSystem.Modules.Stats.Models.TeamStats;
import EloRatingSystem.Modules.Stats.Repositories.Streaks.TeamStreakRepository;
import EloRatingSystem.Modules.Stats.Repositories.TeamStatsRepository;
import EloRatingSystem.Modules.Stats.Utils.StatsUtils;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import EloRatingSystem.Modules.Team.Repositories.TeamPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeamStatsService {

    @Autowired
    TeamStatsRepository teamStatsRepository;
    @Autowired
    TeamPairRepository teamPairRepository;
    @Autowired
    TeamStreakRepository teamStreakRepository;
    @Autowired
    AchievementService achievementService;
    @Autowired
    StatsUtils statsUtils;

    public void updatePlayerStats(TeamPair team, TeamRating rating) {
        Match match = rating.getMatch();
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), team.getPlayerA());
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;

        int currentStreak = 0;

        Optional<TeamStats> teamStatsOptional = teamStatsRepository.findByTeamPairId(team.getId());
        TeamStats stats = teamStatsOptional.orElseGet(() ->
                new TeamStats(
                        team,
                        won ? 1 : 0,
                        !won ? 1 : 0,
                        isBlue ? match.getBlueTeamScore() : match.getRedTeamScore(),
                        rating.getNewRating() > rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        rating.getNewRating() < rating.getOldRating() ? rating.getNewRating() : rating.getOldRating(),
                        won ? 1 : 0,
                        won ? 1 : 0,
                        isBlue && match.getRedTeamScore() == 0 || !isBlue && match.getBlueTeamScore() == 0 ? 1 : 0
                )
        );

        if (teamStatsOptional.isPresent()) {
            if (won) {
                if (statsUtils.tenZeroMatch(match.getBlueTeamScore(), match.getRedTeamScore())) {
                    stats.setShutouts(stats.getShutouts() + 1);
                }
                stats.setWins(stats.getWins() + 1);
                currentStreak = stats.getCurrentWinStreak() + 1;
                stats.setCurrentWinStreak(currentStreak);
                if (stats.getCurrentWinStreak() > stats.getLongestWinStreak()) {
                    stats.setLongestWinStreak(stats.getCurrentWinStreak());
                }
            } else {
                stats.setLost(stats.getLost() + 1);
                stats.setCurrentWinStreak(currentStreak);
            }

            int newRating = rating.getNewRating();
            stats.setHighestELO(Math.max(stats.getHighestELO(), newRating));
            stats.setLowestELO(Math.min(stats.getLowestELO(), newRating));
            stats.setGoals(stats.getGoals() + (isBlue ? match.getBlueTeamScore() : match.getRedTeamScore()));
        }
        teamStreakRepository.save(new TeamStreak(match,team,currentStreak));
        teamStatsRepository.save(stats);
        //achievementService.checkAndUnlockAchievementsSolo(player, match); <--- no team achievements yet
    }

    public void undoTeamStats(TeamPair team, Match match, int highestELO, int lowestELO) {
        boolean isBlue = statsUtils.isPlayerInTeam(match.getBlueTeam(), team.getPlayerA());
        boolean isBlueWinner = statsUtils.isWinner(match.getBlueTeamScore(), match.getRedTeamScore());
        boolean won = isBlue && isBlueWinner || !isBlue && !isBlueWinner;

        TeamStats stats = teamStatsRepository.findByTeamPairId(team.getId())
                .orElseThrow(() -> new RuntimeException("Stats missing for player " + team.getId()));

        if (won) {
            if (statsUtils.tenZeroMatch(match.getBlueTeamScore(), match.getRedTeamScore())) {
                stats.setShutouts(stats.getShutouts() - 1);
            }
            stats.setWins(stats.getWins() - 1);
        } else {
            stats.setLost(stats.getLost() - 1);
        }

        int goals = isBlue ? match.getBlueTeamScore() : match.getRedTeamScore();
        stats.setGoals(stats.getGoals() - goals);

        stats.setLongestWinStreak(getLongestStreakByTeamPairId(team.getId()));
        stats.setCurrentWinStreak(getLatestStreakByTeamPairId(team.getId()));

        stats.setHighestELO(Math.max(highestELO, 1200));
        stats.setLowestELO(Math.min(lowestELO,1200));

        teamStatsRepository.save(stats);
    }

    public int getLongestStreakByTeamPairId(Long teamPairId){
        return teamStreakRepository.findTopByTeamPairIdOrderByWinStreakDesc(teamPairId)
                .map(TeamStreak::getWinStreak)
                .orElse(0);
    }

    public int getLatestStreakByTeamPairId(Long teamPairId){
        return teamStreakRepository.findTopByTeamPairIdOrderByMatchIdDesc(teamPairId)
                .map(TeamStreak::getWinStreak)
                .orElse(0);
    }

    public void deleteStreakByMatchId(Long matchId){
        teamStreakRepository.deleteAllByMatchId(matchId);
    }

    public Mono<List<TeamStatisticsResponseDto>> getAllTeamStatistics() {
        List<TeamPair> teams = teamPairRepository.findAll();
        List<TeamStatisticsResponseDto> teamDtos = new ArrayList<>();

        for (TeamPair team : teams) {
            if (team.getPlayerA().getActive() && team.getPlayerB().getActive()){
                teamDtos.add(getTeamStatisticsByTeamPair(team));
            }
        }

        return Mono.just(teamDtos);
    }

    public Mono<TeamStatisticsResponseDto> getTeamStatisticsByTeamPair(Long teamPairId) {
        Optional<TeamPair> teamOpt = teamPairRepository.findById(teamPairId);
        if (teamOpt.isPresent()) {
            TeamPair team = teamOpt.get();
            return Mono.just(getTeamStatisticsByTeamPair(team));
        } else {
            return Mono.error(new ApiException(String.format("%s Doesn't exist", teamPairId), HttpStatus.BAD_REQUEST));
        }
    }
    private TeamStatisticsResponseDto getTeamStatisticsByTeamPair(TeamPair teamPair) {
        return teamStatsRepository.findCombinedStatsByTeamPairIdAndDate(teamPair.getId(), LocalDate.now())
                .orElse(new TeamStatisticsResponseDto(teamPair,0));
    }
}
