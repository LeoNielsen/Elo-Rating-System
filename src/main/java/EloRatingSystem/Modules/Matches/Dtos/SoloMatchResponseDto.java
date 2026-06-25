package EloRatingSystem.Modules.Matches.Dtos;

import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
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
    private String createdBy;

    public SoloMatchResponseDto(SoloMatch match) {
        this.id = match.getId();
        this.date = match.getDate();
        this.redPlayer = formatPlayerName(match.getRedPlayer());
        this.bluePlayer = formatPlayerName(match.getBluePlayer());
        this.redScore = match.getRedScore();
        this.blueScore = match.getBlueScore();
        this.createdBy = match.getCreatedBy();
    }
    private String formatPlayerName(Player player) {
        return player.getNameTag() + (player.getActive() ? "" : " (Inactive)");
    }
}
