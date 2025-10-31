package EloRatingSystem.Models;

import EloRatingSystem.Models.Match.SoloMatch;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SoloRating")
public class SoloPlayerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private SoloMatch soloMatch;

    @ManyToOne
    private Player player;

    @Column(name = "oldRating", nullable = false)
    private Integer oldRating;

    @Column(name = "newRating", nullable = false)
    private Integer newRating;

    public SoloPlayerRating(SoloMatch soloMatch, Player player, Integer oldRating, Integer newRating){
        this.soloMatch = soloMatch;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }
}
