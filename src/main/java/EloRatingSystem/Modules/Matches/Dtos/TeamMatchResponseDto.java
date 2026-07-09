package EloRatingSystem.Modules.Matches.Dtos;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.player.Models.Player;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class TeamMatchResponseDto {

    private Long id;
    private Date date;
    private String redAtk;
    private String redDef;
    private String blueAtk;
    private String blueDef;
    private Integer redScore;
    private Integer blueScore;
    private String createdBy;

    public TeamMatchResponseDto(Match match) {
        this.id = match.getId();
        this.date = match.getDate();

        this.redAtk = formatPlayerName(match.getRedTeam().getAttacker());
        this.redDef = formatPlayerName(match.getRedTeam().getDefender());
        this.blueAtk = formatPlayerName(match.getBlueTeam().getAttacker());
        this.blueDef = formatPlayerName(match.getBlueTeam().getDefender());

        this.redScore = match.getRedTeamScore();
        this.blueScore = match.getBlueTeamScore();

        this.createdBy = match.getCreatedBy();
    }

    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
