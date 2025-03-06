package EloRatingSystem.Dtos;

import lombok.Data;

@Data
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

    public PlayerStatisticsResponseDto(Long id, String nameTag, Integer rating, Integer attackerWins, Integer defenderWins, Integer attackerLost, Integer defenderLost, Integer totalGoals, Integer todayRatingChance) {
        this.id = id;
        this.nameTag = nameTag;
        this.rating = rating;
        this.attackerWins = attackerWins;
        this.defenderWins = defenderWins;
        this.attackerLost = attackerLost;
        this.defenderLost = defenderLost;
        this.totalGoals = totalGoals;
        this.todayRatingChance = todayRatingChance;
    }
}