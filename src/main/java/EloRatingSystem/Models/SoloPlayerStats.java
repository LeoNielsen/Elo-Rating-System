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
    private Integer wins;
    @Column(name = "lost", nullable = false)
    private Integer lost;
    @Column(name = "goals", nullable = false)
    private Integer goals;
    @Column(name = "highestELO", nullable = false)
    private Integer highestELO;
    @Column(name = "lowestELO", nullable = false)
    private Integer lowestELO;
    @Column(name = "longestWinStreak", nullable = false)
    private Integer longestWinStreak;
    @Column(name = "currentWinStreak", nullable = false)
    private Integer currentWinStreak;

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
}
