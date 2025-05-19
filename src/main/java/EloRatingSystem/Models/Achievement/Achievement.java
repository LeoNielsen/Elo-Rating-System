package EloRatingSystem.Models.Achievement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    private AchievementType type;

    @Enumerated(EnumType.STRING)
    private AchievementMetric metric;

    private int amount;

    public Achievement(String code, String name, String description, GameType gameType, AchievementType type, AchievementMetric metric, int amount) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.gameType = gameType;
        this.type = type;
        this.metric = metric;
        this.amount = amount;
    }
}



