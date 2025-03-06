package EloRatingSystem.Dtos;

import lombok.Data;

@Data
public class SoloPlayerStatisticsResponseDto {
    private Long id;
    private String nameTag;
    private Integer rating;
    private Integer wins;
    private Integer lost;
    private Integer totalGoals;
    private Integer todayRatingChance;

    public SoloPlayerStatisticsResponseDto(Long id, String nameTag, Integer rating, Integer wins, Integer lost, Integer goals,Integer todayRatingChance) {
        this.id = id;
        this.nameTag = nameTag;
        this.rating = rating;
        this.wins = wins;
        this.lost = lost;
        this.totalGoals = goals;
        this.todayRatingChance = todayRatingChance;
    }
}
