package EloRatingSystem.Dtos.MatchDtos;

import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.Match.SoloMatch;
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
        this.redPlayer = formatPlayerName(match.getRedPlayer());
        this.bluePlayer = formatPlayerName(match.getBluePlayer());
        this.redScore = match.getRedScore();
        this.blueScore = match.getBlueScore();
    }
    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
