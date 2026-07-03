package EloRatingSystem.Modules.Stats.Models.Streaks;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamStreak {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private TeamPair teamPair;

    @Column(name = "winStreak", nullable = false)
    private Integer winStreak = 0;

    public TeamStreak(Match match, TeamPair team, Integer winStreak) {
        this.match = match;
        this.teamPair = team;
        this.winStreak = winStreak;
    }
}
