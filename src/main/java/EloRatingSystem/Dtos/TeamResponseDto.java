package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Team;
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
        this.attacker = team.getAttacker().getNameTag();
        this.defender = team.getDefender().getNameTag();
        this.won = team.getWon();
        this.lost = team.getLost();
    }

}
