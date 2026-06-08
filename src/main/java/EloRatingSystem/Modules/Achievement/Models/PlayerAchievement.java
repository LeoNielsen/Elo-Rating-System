package EloRatingSystem.Modules.Achievement.Models;

import EloRatingSystem.Modules.Matches.Models.Match;
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
    private Match teamMatch;

    public PlayerAchievement(Player player, Achievement achievement, boolean unlocked, Date date, Match teamMatch,  SoloMatch soloMatch) {
        this.player = player;
        this.achievement = achievement;
        this.unlocked = unlocked;
        this.gameType = achievement.getGameType();
        this.date = date;
        this.teamMatch = teamMatch;
        this.soloMatch = soloMatch;
    }
}
