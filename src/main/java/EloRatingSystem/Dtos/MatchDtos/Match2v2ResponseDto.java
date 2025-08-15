package EloRatingSystem.Dtos.MatchDtos;

import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Player;
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

        this.redAtk = formatPlayerName(match.getRedTeam().getAttacker());
        this.redDef = formatPlayerName(match.getRedTeam().getDefender());
        this.blueAtk = formatPlayerName(match.getBlueTeam().getAttacker());
        this.blueDef = formatPlayerName(match.getBlueTeam().getDefender());

        this.redScore = match.getRedTeamScore();
        this.blueScore = match.getBlueTeamScore();
    }

    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
