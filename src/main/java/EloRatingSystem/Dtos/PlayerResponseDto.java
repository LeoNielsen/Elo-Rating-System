package EloRatingSystem.Dtos;

import EloRatingSystem.Models.Player;
import lombok.Data;

@Data
public class PlayerResponseDto {

    private Long id;
    private String nameTag;
    private Integer rating;

    public PlayerResponseDto(String nameTag, Integer rating) {
        this.nameTag = nameTag;
        this.rating = rating;
    }

    public PlayerResponseDto(Player player){
        this.id = player.getId();
        this.nameTag = player.getNameTag();
        this.rating = player.getRating();
    }

}
