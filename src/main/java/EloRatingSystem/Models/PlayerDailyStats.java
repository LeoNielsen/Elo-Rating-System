package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PlayerDailyStats")
public class PlayerDailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Player player;
    @Column(name = "date", nullable = false)
    private Date date;
    @Column(name = "ratingChange", nullable = false)
    private int ratingChange;

    public PlayerDailyStats(Player player, Date date, int ratingChange) {
        this.player = player;
        this.date = date;
        this.ratingChange = ratingChange;
    }
}
