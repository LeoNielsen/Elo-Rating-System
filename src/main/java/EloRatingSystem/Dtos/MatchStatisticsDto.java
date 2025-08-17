package EloRatingSystem.Dtos;

import lombok.Data;

@Data
public class MatchStatisticsDto {
    int games;
    int redWins;
    int blueWins;
    int redGoals;
    int blueGoals;
    int goals;

    public MatchStatisticsDto(int redWins, int blueWins, int redGoals, int blueGoals) {
        this.games = redWins + blueWins;
        this.redWins = redWins;
        this.blueWins = blueWins;
        this.redGoals = redGoals;
        this.blueGoals = blueGoals;
        this.goals = redGoals + blueGoals;
    }
}
