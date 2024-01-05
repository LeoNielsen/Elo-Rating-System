package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Match;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchRequestDto {

    private Long redTeamId;
    private Long blueTeamId;
    private Integer redTeamScore;
    private Integer blueTeamScore;

}
