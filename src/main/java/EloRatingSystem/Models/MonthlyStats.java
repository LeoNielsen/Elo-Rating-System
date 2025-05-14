package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MonthlyStats")
public class MonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Player player;

    @Column(name = "MonthlyRating", nullable = false)
    private Integer MonthlyRating;
    @Column(name = "attackerWins", nullable = false)
    private Integer attackerWins;
    @Column(name = "defenderWins", nullable = false)
    private Integer defenderWins;
    @Column(name = "attackerLost", nullable = false)
    private Integer attackerLost;
    @Column(name = "defenderLost", nullable = false)
    private Integer defenderLost;
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

    public MonthlyStats(Player player, Integer attackerWins, Integer defenderWins, Integer attackerLost, Integer defenderLost, Integer goals, Integer highestELO, Integer lowestELO, Integer longestWinStreak, Integer currentWinStreak) {
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

}
