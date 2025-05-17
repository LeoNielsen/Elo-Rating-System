package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PlayerStats")
public class PlayerStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Player player;

    @Column(name = "attackerWins", nullable = false)
    private Integer attackerWins = 0;
    @Column(name = "defenderWins", nullable = false)
    private Integer defenderWins = 0;
    @Column(name = "attackerLost", nullable = false)
    private Integer attackerLost = 0;
    @Column(name = "defenderLost", nullable = false)
    private Integer defenderLost = 0;
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

    public PlayerStats(Player player, Integer attackerWins, Integer defenderWins, Integer attackerLost, Integer defenderLost, Integer goals, Integer highestELO, Integer lowestELO, Integer longestWinStreak, Integer currentWinStreak) {
        this.player = player;
        this.attackerWins = attackerWins;
        this.defenderWins = defenderWins;
        this.attackerLost = attackerLost;
        this.defenderLost = defenderLost;
        this.goals = goals;
        this.highestELO = highestELO;
        this.lowestELO = lowestELO;
        this.longestWinStreak = longestWinStreak;
        this.currentWinStreak = currentWinStreak;
    }

    public PlayerStats(Player player) {
        this.player = player;
    }

}
