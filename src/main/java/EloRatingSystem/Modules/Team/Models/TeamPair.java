package EloRatingSystem.Modules.Team.Models;

import EloRatingSystem.Modules.player.Models.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "teamPair")
public class TeamPair {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Player playerA;

    @ManyToOne
    private Player playerB;

    private int rating;

    @OneToMany(mappedBy = "pair")
    private Set<Team> teams = new HashSet<>();

    public TeamPair(Player p1, Player p2, int rating) {
        if (p1.getId() < p2.getId()) {
            this.playerA = p1;
            this.playerB = p2;
        } else {
            this.playerA = p2;
            this.playerB = p1;
        }
        this.rating = rating;
        this.teams = new HashSet<>();
    }
}
