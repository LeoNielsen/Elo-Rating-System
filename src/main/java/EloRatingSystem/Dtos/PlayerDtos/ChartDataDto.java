package EloRatingSystem.Dtos.PlayerDtos;

import EloRatingSystem.Models.DailyStats.DailyStatsAbstract;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChartDataDto {

    private String playerTag;
    private Integer rating;
    private LocalDate date;

    public ChartDataDto(DailyStatsAbstract dailyStatsAbstract){
        this.playerTag = dailyStatsAbstract.getPlayer().getNameTag();
        this.rating = dailyStatsAbstract.getRating();
        this.date = dailyStatsAbstract.getDate();
    }
}
