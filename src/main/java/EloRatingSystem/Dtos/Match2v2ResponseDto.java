package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Match;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class Match2v2ResponseDto {

    private Long id;
    private Date date;
    private String redAttacker;
    private String redDefender;
    private String blueAttacker;
    private String blueDefender;
    private Integer redTeamScore;
    private Integer blueTeamScore;

    public Match2v2ResponseDto(Match match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redAttacker = match.getRedTeam().getAttacker().getNameTag();
        this.redDefender = match.getRedTeam().getDefender().getNameTag();
        this.blueAttacker = match.getBlueTeam().getAttacker().getNameTag();
        this.blueDefender = match.getBlueTeam().getDefender().getNameTag();
        this.redTeamScore = match.getRedTeamScore();
        this.blueTeamScore = match.getBlueTeamScore();
    }
}
