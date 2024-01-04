package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Team")
public class Team {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Player attacker;

    @ManyToOne
    private Player defender;

    @Column(name = "won")
    private Integer won;

    @Column(name = "lost")
    private Integer lost;

    public Team(Player attacker, Player defender){
        this.attacker = attacker;
        this.defender = defender;
        this.won = 0;
        this.lost = 0;
    }

}
