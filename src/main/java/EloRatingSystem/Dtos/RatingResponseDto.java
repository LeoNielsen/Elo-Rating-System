package EloRatingSystem.Dtos;

import EloRatingSystem.Models.PlayerRating;
import lombok.Data;
@Data
public class RatingResponseDto {

    private Long matchId;
    private PlayerResponseDto player;
    private Integer oldRating;
    private Integer newRating;

    public RatingResponseDto(Long matchId, PlayerResponseDto player, Integer oldRating, Integer newRating) {
        this.matchId = matchId;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }

    public RatingResponseDto(PlayerRating rating) {
        this.matchId = rating.getMatch().getId();
        this.player = new PlayerResponseDto(rating.getPlayer());
        this.oldRating = rating.getOldRating();
        this.newRating = rating.getNewRating();
    }

}
