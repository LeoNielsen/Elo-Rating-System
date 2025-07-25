package EloRatingSystem.Models.DailyStats;

import EloRatingSystem.Models.Player;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class PlayerDailyStats extends DailyStatsAbstract {

    public PlayerDailyStats(Player player, LocalDate date, int ratingChange, int rating) {
        super(player, date, ratingChange, rating);
    }
}
