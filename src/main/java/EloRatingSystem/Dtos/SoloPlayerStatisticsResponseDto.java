package EloRatingSystem.Dtos;

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
    private Integer todayRatingChance;
    private Integer highestELO;
    private Integer lowestELO;
    private Integer longestWinStreak;
    private Integer currentWinStreak;


}
