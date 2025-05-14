package EloRatingSystem.Dtos;

import EloRatingSystem.Models.MonthlyRating;
import EloRatingSystem.Models.PlayerRating;
import EloRatingSystem.Models.SoloPlayerRating;
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
    public RatingResponseDto(MonthlyRating rating) {
        this.matchId = rating.getMatch().getId();
        this.player = new PlayerResponseDto(rating.getPlayer());
        this.oldRating = rating.getOldRating();
        this.newRating = rating.getNewRating();
    }
    public RatingResponseDto(SoloPlayerRating rating) {
        this.matchId = rating.getSoloMatch().getId();
        this.player = new PlayerResponseDto(rating.getPlayer());
        this.oldRating = rating.getOldRating();
        this.newRating = rating.getNewRating();
    }


}
