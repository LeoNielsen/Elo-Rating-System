package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MonthlyWinners")
public class MonthlyWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Player player;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "monthlyRating", nullable = false)
    private Integer monthlyRating;

    public MonthlyWinner(Player player, Integer year, Integer month, Integer monthlyRating) {
        this.player = player;
        this.year = year;
        this.month = month;
        this.monthlyRating = monthlyRating;
    }
}
