package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Team;
import lombok.Data;

@Data
public class TeamResponseDto {

    private String attacker;

    private String defender;

    private Integer won;

    private Integer lost;

    public TeamResponseDto(Team team){
        this.attacker = team.getAttacker().getNameTag();
        this.defender = team.getDefender().getNameTag();
        this.won = team.getWon();
        this.lost = team.getLost();
    }

}
