package EloRatingSystem.Dtos;

import EloRatingSystem.Models.SoloMatch;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class SoloMatchResponseDto {
    private Long id;
    private Date date;
    private PlayerResponseDto redPlayer;
    private PlayerResponseDto bluePlayer;
    private Integer redTeamScore;
    private Integer blueTeamScore;

    public SoloMatchResponseDto(SoloMatch match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redPlayer = new PlayerResponseDto(match.getRedPlayer());
        this.bluePlayer = new PlayerResponseDto(match.getBluePlayer());
        this.redTeamScore = match.getRedScore();
        this.blueTeamScore = match.getBlueScore();
    }
}
