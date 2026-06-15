package EloRatingSystem.Modules.Achievement.Models;

import EloRatingSystem.Modules.Matches.Models.TeamMatch;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.player.Models.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player;

    @ManyToOne
    private Achievement achievement;

    private boolean unlocked;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    private Date date;

    @ManyToOne
    private SoloMatch soloMatch;

    @ManyToOne
    private TeamMatch match;

    public PlayerAchievement(Player player, Achievement achievement, boolean unlocked, Date date, TeamMatch match, SoloMatch soloMatch) {
        this.player = player;
        this.achievement = achievement;
        this.unlocked = unlocked;
        this.gameType = achievement.getGameType();
        this.date = date;
        this.match = match;
        this.soloMatch = soloMatch;
    }
}
