package EloRatingSystem.Dtos;

import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Models.Team;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamResponseDto {

    private Long id;
    private PlayerResponseDto attacker;
    private PlayerResponseDto defender;
    private Integer won;
    private Integer lost;

    public TeamResponseDto(Team team){
        this.id = team.getId();
        this.attacker = new PlayerResponseDto(team.getAttacker());
        this.defender = new PlayerResponseDto(team.getDefender());
        this.won = team.getWon();
        this.lost = team.getLost();
    }

}
