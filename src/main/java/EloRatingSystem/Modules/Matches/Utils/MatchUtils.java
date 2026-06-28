package EloRatingSystem.Modules.Matches.Utils;

import EloRatingSystem.Modules.Achievement.Repositories.PlayerAchievementRepository;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import EloRatingSystem.Modules.Stats.Services.MonthlyStatsService;
import EloRatingSystem.Modules.Stats.Services.SoloStatsService;
import EloRatingSystem.Modules.Stats.Services.StatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.player.Models.Player;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MatchUtils {

    @Autowired
    StatsService statsService;
    @Autowired
    SoloStatsService soloStatsService;
    @Autowired
    MonthlyStatsService monthlyStatsService;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    RatingService ratingService;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    PlayerAchievementRepository playerAchievementRepository;
    public void removeMatchStats(Match match) {
        ratingService.deleteRatingsByMatch(match.getDate().toLocalDate(), match.getId());
        monthlyRatingService.deleteRatingsByMatch(match.getDate().toLocalDate(), match.getId());

        Team winner = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getRedTeam() : match.getBlueTeam();
        Team loser = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getBlueTeam() : match.getRedTeam();

        winner.setWon(winner.getWon() - 1);
        loser.setLost(loser.getLost() - 1);
        winner.setGoals(winner.getGoals() - Math.max(match.getRedTeamScore(),match.getBlueTeamScore()));
        loser.setGoals(loser.getGoals() - Math.min(match.getRedTeamScore(),match.getBlueTeamScore()));

        teamRepository.save(winner);
        teamRepository.save(loser);

        playerAchievementRepository.deleteAllByMatchId(match.getId());

        statsService.deleteStreakByMatchId(match.getId());
        monthlyStatsService.deleteStreakByMatchId(match.getId());

        List<Player> players = new ArrayList<>(Arrays.asList(
                winner.getAttacker(),
                winner.getDefender(),
                loser.getAttacker(),
                loser.getDefender()
        ));

        int month = match.getDate().toLocalDate().getMonthValue();
        int year = match.getDate().toLocalDate().getYear();

        for (Player player : players) {
            statsService.undoPlayerStats(player, match, ratingService.getHighestELOByPlayerId(player.getId()), ratingService.getLowestELOByPlayerId(player.getId()));
            monthlyStatsService.undoPlayerStats(player, match, monthlyRatingService.getHighestELOByPlayerId(player.getId(), month, year), monthlyRatingService.getLowestELOByPlayerId(player.getId(), month, year), month, year);
        }
    }

    @Transactional
    public void removeMatchStats(SoloMatch match) {
        soloRatingService.deleteRatingsBySoloMatch(match.getDate().toLocalDate(), match.getId());

        Player redPlayer = match.getRedPlayer();
        Player bluePlayer = match.getBluePlayer();

        playerAchievementRepository.deleteAllBySoloMatchId(match.getId());

        soloStatsService.deleteStreakByMatchId(match.getId());

        List<Player> players = new ArrayList<>(Arrays.asList(
                redPlayer,
                bluePlayer
        ));

        for (Player player : players) {
            soloStatsService.undoPlayerStats(player, match, soloRatingService.getHighestELOByPlayerId(player.getId()), soloRatingService.getLowestELOByPlayerId(player.getId()));
        }
    }
}
