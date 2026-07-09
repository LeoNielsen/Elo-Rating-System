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
public class MonthlyPlayerStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @ManyToOne
    private Player player;

    @Column(name = "winStreak", nullable = false)
    private Integer winStreak = 0;

    public MonthlyPlayerStreak(Match match, Integer year, Integer month, Player player, Integer winStreak) {
        this.match = match;
        this.year = year;
        this.month = month;
        this.player = player;
        this.winStreak = winStreak;
    }
}
