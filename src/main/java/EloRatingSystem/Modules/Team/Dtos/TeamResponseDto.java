package EloRatingSystem.Modules.Team.Dtos;

import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.Team.Models.Team;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamResponseDto {

    private Long id;
    private String attacker;
    private String defender;
    private Integer won;
    private Integer lost;

    public TeamResponseDto(Team team){
        this.id = team.getId();
        this.attacker = formatPlayerName(team.getAttacker());
        this.defender = formatPlayerName(team.getDefender());
        this.won = team.getWon();
        this.lost = team.getLost();
    }
    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
