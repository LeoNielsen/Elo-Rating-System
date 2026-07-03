package EloRatingSystem.Modules.Stats.Models;

import EloRatingSystem.Modules.Team.Models.TeamPair;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private TeamPair teamPair;

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

    public TeamStats(TeamPair team, Integer wins, Integer lost, Integer goals, Integer highestELO, Integer lowestELO, Integer longestWinStreak, Integer currentWinStreak, Integer shutouts) {
        this.teamPair = team;
        this.wins = wins;
        this.lost = lost;
        this.goals = goals;
        this.highestELO = highestELO;
        this.lowestELO = lowestELO;
        this.longestWinStreak = longestWinStreak;
        this.currentWinStreak = currentWinStreak;
        this.shutouts = shutouts;
    }

    @Column(name = "currentWinStreak", nullable = false)
    private Integer currentWinStreak = 0;
    @Column(name = "shutouts", nullable = false)
    private Integer shutouts = 0;


}
