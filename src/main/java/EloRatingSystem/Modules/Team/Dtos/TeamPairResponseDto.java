package EloRatingSystem.Modules.Team.Dtos;

import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import EloRatingSystem.Modules.player.Models.Player;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TeamPairResponseDto {

    private Long id;
    private String playerA;
    private String playerB;
    private Integer rating;
    private Integer won;
    private Integer lost;
    private Integer goals;
    private Integer shutouts;
    private List<TeamResponseDto> teams;

    public TeamPairResponseDto(TeamPair pair){
        this.id = pair.getId();
        this.playerA = formatPlayerName(pair.getPlayerA());
        this.playerB = formatPlayerName(pair.getPlayerB());
        this.rating = pair.getRating();
        this.won = 0;
        this.lost = 0;
        this.goals = 0;
        this.shutouts = 0;
        this.teams = new ArrayList<>();
        for(Team t:pair.getTeams()){
            this.teams.add(new TeamResponseDto(t));
            this.goals += t.getGoals();
            this.won += t.getWon();
            this.lost += t.getLost();
            this.shutouts += t.getShutouts();
        }
    }
    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
