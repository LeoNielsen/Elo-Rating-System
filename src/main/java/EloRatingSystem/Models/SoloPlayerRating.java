package EloRatingSystem.Models;

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

    @Column(name = "oldRating")
    private Integer oldRating;

    @Column(name = "newRating")
    private Integer newRating;

    public SoloPlayerRating(SoloMatch soloMatch, Player player, Integer oldRating, Integer newRating){
        this.soloMatch = soloMatch;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }
}
