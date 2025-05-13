package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.PlayerStats;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerStatisticsResponseDto {

    private Long id;
    private String nameTag;
    private Integer rating;
    private Integer attackerWins;
    private Integer defenderWins;
    private Integer attackerLost;
    private Integer defenderLost;
    private Integer totalGoals;
    private Integer todayRatingChance;
    private Integer highestELO;
    private Integer lowestELO;
    private Integer longestWinStreak;
    private Integer currentWinStreak;

    public PlayerStatisticsResponseDto(Player player, PlayerStats playerStats, int todayRatingChance) {
        this.id = player.getId();
        this.nameTag = player.getNameTag();
        this.rating = player.getRating();
        this.attackerWins = playerStats.getAttackerWins();
        this.defenderWins = playerStats.getDefenderWins();
        this.attackerLost = playerStats.getAttackerLost();
        this.defenderLost = playerStats.getDefenderLost();
        this.totalGoals = playerStats.getGoals();
        this.highestELO = playerStats.getHighestELO();
        this.lowestELO = playerStats.getLowestELO();
        this.longestWinStreak = playerStats.getLongestWinStreak();
        this.currentWinStreak = playerStats.getCurrentWinStreak();
        this.todayRatingChance = todayRatingChance;
    }
}