package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Team;
import lombok.Data;

@Data
public class TeamResponseDto {

    private Long id;
    private Long attackerId;
    private Long defenderId;
    private Integer won;
    private Integer lost;

    public TeamResponseDto(Team team){
        this.id = team.getId();
        this.attackerId = team.getAttacker().getId();
        this.defenderId = team.getDefender().getId();
        this.won = team.getWon();
        this.lost = team.getLost();
    }

}
