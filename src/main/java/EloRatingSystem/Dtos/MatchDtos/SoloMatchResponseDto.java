package EloRatingSystem.Dtos.MatchDtos;

import EloRatingSystem.Models.SoloMatch;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class SoloMatchResponseDto {
    private Long id;
    private Date date;
    private String redPlayer;
    private String bluePlayer;
    private Integer redScore;
    private Integer blueScore;

    public SoloMatchResponseDto(SoloMatch match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redPlayer = match.getRedPlayer().getNameTag();
        this.bluePlayer = match.getBluePlayer().getNameTag();
        this.redScore = match.getRedScore();
        this.blueScore = match.getBlueScore();
    }
}
