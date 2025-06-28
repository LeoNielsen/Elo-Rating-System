package EloRatingSystem.Models.DailyStats;

import EloRatingSystem.Models.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DailyStatsAbstract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Player player;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "ratingChange", nullable = false)
    private int ratingChange;
    @Column(name = "Rating", nullable = false)
    private int rating;

    public DailyStatsAbstract(Player player, LocalDate date, int ratingChange,int rating) {
        this.player = player;
        this.date = date;
        this.ratingChange = ratingChange;
        this.rating = rating;
    }
}
