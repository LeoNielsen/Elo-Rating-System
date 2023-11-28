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

    @Column(name = "nameTag")
    private String nameTag;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "active")
    private Boolean active;

    public Player(String nameTag, Integer rating, Boolean active) {
        this.nameTag = nameTag;
        this.rating = rating;
        this.active = active;
    }


}
