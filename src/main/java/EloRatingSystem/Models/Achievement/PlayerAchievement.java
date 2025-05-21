package EloRatingSystem.Models.Achievement;

import EloRatingSystem.Models.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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




    public PlayerAchievement(Player player, Achievement achievement, boolean unlocked) {
        this.player = player;
        this.achievement = achievement;
        this.unlocked = unlocked;
        this.gameType = achievement.getGameType();
    }
}
