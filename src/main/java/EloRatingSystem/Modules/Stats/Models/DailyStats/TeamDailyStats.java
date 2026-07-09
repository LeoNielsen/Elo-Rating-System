package EloRatingSystem.Modules.Stats.Models.DailyStats;

import EloRatingSystem.Modules.Team.Models.TeamPair;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Data
public class TeamDailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private TeamPair teamPair;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "ratingChange", nullable = false)
    private int ratingChange = 0;
    @Column(name = "Rating", nullable = false)
    private int rating = 0;

    public TeamDailyStats(TeamPair team, LocalDate date, int ratingChange, int rating) {
        this.teamPair = team;
        this.date = date;
        this.ratingChange = ratingChange;
        this.rating = rating;
    }


}

