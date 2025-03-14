package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "nameTag", nullable = false, unique = true)
    private String nameTag;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "soloRating", nullable = false)
    private Integer soloRating;

    @Column(name = "active", nullable = false)
    private Boolean active;

    public Player(String nameTag, Integer rating, Integer soloRating, Boolean active) {
        this.nameTag = nameTag;
        this.rating = rating;
        this.soloRating = soloRating;
        this.active = active;
    }

}
