package EloRatingSystem.Dtos.PlayerDtos;

import EloRatingSystem.Models.MonthlyWinner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWinnerDto {
    private String nameTag;
    private Integer year;
    private Integer month;
    private Integer monthlyRating;

    public MonthlyWinnerDto(MonthlyWinner monthlyWinner) {
        if (monthlyWinner.getPlayer() != null) {
            this.nameTag = monthlyWinner.getPlayer().getNameTag();
            this.year = monthlyWinner.getYear();
            this.month = monthlyWinner.getMonth();
            this.monthlyRating = monthlyWinner.getMonthlyRating();
        }
    }

}
