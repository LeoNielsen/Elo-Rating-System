package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rating")
public class PlayerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private Player player;

    @Column(name = "oldRating")
    private Integer oldRating;

    @Column(name = "newRating")
    private Integer newRating;

    public PlayerRating(Match match, Player player, Integer oldRating, Integer newRating){
        this.match = match;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }
}
