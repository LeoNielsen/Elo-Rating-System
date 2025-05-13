package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SoloMatch")
public class SoloMatch {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "date", nullable = false)
    private Date date;

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Player redPlayer;

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Player bluePlayer;

    @Column(name = "redScore", nullable = false)
    private Integer redScore;

    @Column(name = "blueScore", nullable = false)
    private Integer blueScore;

    public SoloMatch(Date date, Player redPlayer, Player bluePlayer, Integer redTeamScore, Integer blueTeamScore) {
        this.date = date;
        this.redPlayer = redPlayer;
        this.bluePlayer = bluePlayer;
        this.redScore = redTeamScore;
        this.blueScore = blueTeamScore;
    }
}
