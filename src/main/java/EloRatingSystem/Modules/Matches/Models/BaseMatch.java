package EloRatingSystem.Modules.Matches.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class BaseMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Integer redScore;

    @Column(nullable = false)
    private Integer blueScore;

    public BaseMatch(Date date, Integer redScore, Integer blueScore) {
        this.date = date;
        this.redScore = redScore;
        this.blueScore = blueScore;
    }
}
