package EloRatingSystem.Modules.Stats.Dtos;

import EloRatingSystem.Modules.Stats.Models.TeamStats;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import EloRatingSystem.Modules.player.Models.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamStatisticsResponseDto {
    private Long id;
    private String teamName;
    private Integer rating;
    private Integer wins;
    private Integer lost;
    private Integer totalGoals;
    private Integer highestELO;
    private Integer lowestELO;
    private Integer longestWinStreak;
    private Integer currentWinStreak;
    private Integer todayRatingChance;
    private Integer shutouts;


    public TeamStatisticsResponseDto(TeamPair team, TeamStats teamStats, int todayRatingChance) {
        this.id = team.getId();
        this.teamName = formatTeamName(team);
        this.rating = team.getRating();
        this.wins = teamStats.getWins();
        this.lost = teamStats.getLost();
        this.totalGoals = teamStats.getGoals();
        this.highestELO = teamStats.getHighestELO();
        this.lowestELO = teamStats.getLowestELO();
        this.longestWinStreak = teamStats.getLongestWinStreak();
        this.currentWinStreak = teamStats.getCurrentWinStreak();
        this.todayRatingChance = todayRatingChance;
        this.shutouts = teamStats.getShutouts();
    }

    public TeamStatisticsResponseDto(TeamPair team, int todayRatingChance) {
        this.id = team.getId();
        this.teamName = formatTeamName(team);
        this.rating = team.getRating();
        this.wins = 0;
        this.lost = 0;
        this.totalGoals = 0;
        this.highestELO = 1200;
        this.lowestELO = 1200;
        this.longestWinStreak = 0;
        this.currentWinStreak = 0;
        this.todayRatingChance = todayRatingChance;
        this.shutouts = 0;
    }
    private String formatTeamName(TeamPair team) {
        Player playerA = team.getPlayerA();
        Player playerB = team.getPlayerB();
        return playerA.getNameTag() + (playerA.getActive() ? "" : " (Inactive)") + " - "
                + playerB.getNameTag() + (playerB.getActive() ? "" : " (Inactive)");
    }
}
