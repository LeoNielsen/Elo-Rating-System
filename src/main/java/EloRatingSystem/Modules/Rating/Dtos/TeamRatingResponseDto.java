package EloRatingSystem.Modules.Rating.Dtos;

import EloRatingSystem.Modules.Rating.Models.TeamRating;
import EloRatingSystem.Modules.Team.Dtos.TeamPairResponseDto;
import lombok.Data;
@Data
public class TeamRatingResponseDto {

    private Long matchId;
    private TeamPairResponseDto team;
    private Integer oldRating;
    private Integer newRating;

    public TeamRatingResponseDto(Long matchId, TeamPairResponseDto team, Integer oldRating, Integer newRating) {
        this.matchId = matchId;
        this.team = team;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }

    public TeamRatingResponseDto(TeamRating rating) {
        this.matchId = rating.getMatch().getId();
        this.team = new TeamPairResponseDto(rating.getTeamPair());
        this.oldRating = rating.getOldRating();
        this.newRating = rating.getNewRating();
    }

}
