package EloRatingSystem.Modules.Matches.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMatchRequestDto {

    private Long redAtkId;
    private Long redDefId;
    private Long blueAtkId;
    private Long blueDefId;
    private Integer redScore;
    private Integer blueScore;

}