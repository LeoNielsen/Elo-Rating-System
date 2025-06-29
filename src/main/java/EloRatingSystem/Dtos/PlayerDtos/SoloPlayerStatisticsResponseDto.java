package EloRatingSystem.Dtos.PlayerDtos;

import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloPlayerStats;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoloPlayerStatisticsResponseDto {
    private Long id;
    private String nameTag;
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


    public SoloPlayerStatisticsResponseDto(Player player, SoloPlayerStats playerStats, int todayRatingChance) {
        this.id = player.getId();
        this.nameTag = player.getNameTag();
        this.rating = player.getSoloRating();
        this.wins = playerStats.getWins();
        this.lost = playerStats.getLost();
        this.totalGoals = playerStats.getGoals();
        this.highestELO = playerStats.getHighestELO();
        this.lowestELO = playerStats.getLowestELO();
        this.longestWinStreak = playerStats.getLongestWinStreak();
        this.currentWinStreak = playerStats.getCurrentWinStreak();
        this.todayRatingChance = todayRatingChance;
        this.shutouts = playerStats.getShutouts();
    }

    public SoloPlayerStatisticsResponseDto(Player player, int todayRatingChance) {
        this.id = player.getId();
        this.nameTag = player.getNameTag();
        this.rating = player.getSoloRating();
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

}
