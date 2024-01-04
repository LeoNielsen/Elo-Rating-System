package EloRatingSystem.Dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlayerRequestDto {

    String nameTag;
    public PlayerRequestDto(String nameTag){
        this.nameTag = nameTag;
    }
}
