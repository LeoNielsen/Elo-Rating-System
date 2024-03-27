package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Player;
import lombok.Data;

@Data
public class PlayerStatisticsResponseDto {

    private String nameTag;
    private Integer rating;
    private Integer totalWins;
    private Integer totalLost;
    private Integer totalGoals;

    public PlayerStatisticsResponseDto(String nameTag, Integer rating, Integer totalWins, Integer totalLost, Integer totalGoals) {
        this.nameTag = nameTag;
        this.rating = rating;
        this.totalWins = totalWins;
        this.totalLost = totalLost;
        this.totalGoals = totalGoals;
    }
}