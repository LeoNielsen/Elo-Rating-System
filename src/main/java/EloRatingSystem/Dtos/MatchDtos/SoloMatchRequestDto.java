package EloRatingSystem.Dtos.MatchDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloMatchRequestDto {
    private Long redPlayerId;
    private Long bluePlayerId;
    private Integer redScore;
    private Integer blueScore;
}
