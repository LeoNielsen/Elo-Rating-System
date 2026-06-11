package EloRatingSystem.Modules.Stats.Models.Streaks;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.player.Models.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private Player player;

    @Column(name = "winStreak", nullable = false)
    private Integer winStreak = 0;

    public PlayerStreak(Match match, Player player, Integer winStreak) {
        this.match = match;
        this.player = player;
        this.winStreak = winStreak;
    }

}
