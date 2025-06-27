package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Player;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Player2v2ResponseDto {

    private Long id;
    private String nameTag;
    private Integer rating;

    public Player2v2ResponseDto(Player player){
        this.id = player.getId();
        this.nameTag = player.getNameTag();
        this.rating = player.getRating();
    }

}
