package EloRatingSystem.Modules.Rating.Models;

import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.Matches.Models.Match;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MonthlyRating")
public class MonthlyRating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private Player player;

    @Column(name = "oldRating", nullable = false)
    private Integer oldRating;

    @Column(name = "newRating", nullable = false)
    private Integer newRating;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    public MonthlyRating(Match match, Player player, Integer oldRating, Integer newRating, Integer year, Integer month) {
        this.match = match;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
        this.year = year;
        this.month = month;
    }

}
