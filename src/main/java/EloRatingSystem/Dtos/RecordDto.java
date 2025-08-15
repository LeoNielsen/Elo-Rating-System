package EloRatingSystem.Dtos;

import lombok.Data;

@Data
public class RecordDto {
    private String nameTag;
    private int amount;

    public RecordDto(String nameTag, int amount) {
        this.nameTag = nameTag;
        this.amount = amount;
    }
}
