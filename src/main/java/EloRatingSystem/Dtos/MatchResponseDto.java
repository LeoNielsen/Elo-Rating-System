package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Match;

import lombok.Data;

import java.sql.Date;

@Data
public class MatchResponseDto {

    private Long id;
    private Date date;
    private TeamResponseDto redTeam;
    private TeamResponseDto blueTeam;
    private Integer redTeamScore;
    private Integer blueTeamScore;

    public MatchResponseDto(Match match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redTeam = new TeamResponseDto(match.getRedTeam());
        this.blueTeam = new TeamResponseDto(match.getBlueTeam());
        this.redTeamScore = match.getRedTeamScore();
        this.blueTeamScore = match.getBlueTeamScore();
    }
}
