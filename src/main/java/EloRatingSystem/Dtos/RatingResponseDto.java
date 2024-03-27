package EloRatingSystem.Dtos;

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
}
