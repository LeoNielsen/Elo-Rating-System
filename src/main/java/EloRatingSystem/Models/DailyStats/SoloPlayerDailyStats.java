package EloRatingSystem.Models.DailyStats;

import EloRatingSystem.Models.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@NoArgsConstructor
@Table(name = "SoloPlayerDailyStats")
public class SoloPlayerDailyStats extends DailyStatsAbstract {
    public SoloPlayerDailyStats(Player player, Date date, int ratingChange) {
        super(player, date, ratingChange);
    }
}
