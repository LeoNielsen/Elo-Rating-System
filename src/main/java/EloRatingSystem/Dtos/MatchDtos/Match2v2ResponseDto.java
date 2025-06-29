package EloRatingSystem.Dtos.MatchDtos;

import EloRatingSystem.Models.Match;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class Match2v2ResponseDto {

    private Long id;
    private Date date;
    private String redAtk;
    private String redDef;
    private String blueAtk;
    private String blueDef;
    private Integer redScore;
    private Integer blueScore;

    public Match2v2ResponseDto(Match match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redAtk = match.getRedTeam().getAttacker().getNameTag();
        this.redDef = match.getRedTeam().getDefender().getNameTag();
        this.blueAtk = match.getBlueTeam().getAttacker().getNameTag();
        this.blueDef = match.getBlueTeam().getDefender().getNameTag();
        this.redScore = match.getRedTeamScore();
        this.blueScore = match.getBlueTeamScore();
    }
}
