package EloRatingSystem.Dtos;

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
}