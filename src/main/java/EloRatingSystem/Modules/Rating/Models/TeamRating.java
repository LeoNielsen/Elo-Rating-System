package EloRatingSystem.Modules.Rating.Models;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "teamRating")
public class TeamRating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private TeamPair teamPair;

    @Column(name = "oldRating", nullable = false)
    private Integer oldRating;

    @Column(name = "newRating", nullable = false)
    private Integer newRating;

    public TeamRating(Match match, TeamPair team, Integer oldRating, Integer newRating){
        this.match = match;
        this.teamPair = team;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }
}
