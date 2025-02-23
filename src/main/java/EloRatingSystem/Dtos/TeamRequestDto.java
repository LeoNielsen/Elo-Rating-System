package EloRatingSystem.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamRequestDto {

    private Long attackerId;
    private Long defenderId;
}
