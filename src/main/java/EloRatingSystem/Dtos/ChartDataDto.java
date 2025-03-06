package EloRatingSystem.Dtos;

import lombok.Data;

import java.sql.Date;

@Data
public class ChartDataDto {

    private Long matchId;
    private PlayerResponseDto player;
    private Integer newRating;
    private Date date;

    public ChartDataDto(Long matchId, PlayerResponseDto player, Integer newRating, Date date) {
        this.matchId = matchId;
        this.player = player;
        this.newRating = newRating;
        this.date = date;
    }
}
