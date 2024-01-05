package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Match;

import lombok.Data;

@Data
public class MatchResponseDto {

    private Long id;
    private Long redTeamId;
    private Long blueTeamId;
    private Integer redTeamScore;
    private Integer blueTeamScore;

    public MatchResponseDto(Match match) {
        this.id = match.getId();
        this.redTeamId = match.getRedTeam().getId();
        this.blueTeamId = match.getBlueTeam().getId();
        this.redTeamScore = match.getRedTeamScore();
        this.blueTeamScore = match.getBlueTeamScore();
    }
}
