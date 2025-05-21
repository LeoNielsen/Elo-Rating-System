package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SoloPlayerStats")
public class SoloPlayerStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Player player;

    @Column(name = "wins", nullable = false)
    private Integer wins = 0;
    @Column(name = "lost", nullable = false)
    private Integer lost = 0;
    @Column(name = "goals", nullable = false)
    private Integer goals = 0;
    @Column(name = "highestELO", nullable = false)
    private Integer highestELO = 1200;
    @Column(name = "lowestELO", nullable = false)
    private Integer lowestELO = 1200;
    @Column(name = "longestWinStreak", nullable = false)
    private Integer longestWinStreak = 0;
    @Column(name = "currentWinStreak", nullable = false)
    private Integer currentWinStreak = 0;

    public SoloPlayerStats(Player player, Integer wins, Integer lost, Integer goals, Integer highestELO, Integer lowestELO, Integer longestWinStreak, Integer currentWinStreak) {
        this.player = player;
        this.wins = wins;
        this.lost = lost;
        this.goals = goals;
        this.highestELO = highestELO;
        this.lowestELO = lowestELO;
        this.longestWinStreak = longestWinStreak;
        this.currentWinStreak = currentWinStreak;
    }

    public SoloPlayerStats(Player player) {
        this.player = player;
    }
}
