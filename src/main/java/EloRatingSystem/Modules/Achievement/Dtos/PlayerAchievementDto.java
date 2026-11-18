package EloRatingSystem.Modules.Achievement.Dtos;

import EloRatingSystem.Modules.Achievement.Models.Achievement;
import EloRatingSystem.Modules.Achievement.Models.GameType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAchievementDto {

    private Long id;

    private Long playerId;

    private Achievement achievement;

    private boolean unlocked;

    private GameType gameType;

    private Date date;

    private Long soloMatchId;

    private Long matchId;
}
