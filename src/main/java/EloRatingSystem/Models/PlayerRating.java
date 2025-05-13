package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Rating")
public class PlayerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Match match;

    @ManyToOne
    private Player player;

    @Column(name = "oldRating", nullable = false)
    private Integer oldRating;

    @Column(name = "newRating", nullable = false)
    private Integer newRating;

    public PlayerRating(Match match, Player player, Integer oldRating, Integer newRating){
        this.match = match;
        this.player = player;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }
}
