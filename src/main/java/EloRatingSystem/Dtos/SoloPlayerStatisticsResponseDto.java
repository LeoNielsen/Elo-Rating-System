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
    public SoloPlayerStatisticsResponseDto(Long id, String nameTag, Integer rating, int wins, int lost, int goals) {
        this.id = id;
        this.nameTag = nameTag;
        this.rating = rating;
        this.wins = wins;
        this.lost = lost;
        this.totalGoals = goals;

    }
}
