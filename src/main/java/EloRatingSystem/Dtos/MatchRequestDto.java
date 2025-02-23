package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestDto {

    private Long redTeamId;
    private Long blueTeamId;
    private Integer redTeamScore;
    private Integer blueTeamScore;

}
