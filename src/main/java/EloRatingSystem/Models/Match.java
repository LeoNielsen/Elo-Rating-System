package EloRatingSystem.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Match")
public class Match {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Team redTeam;

    @ManyToOne
    private Team blueTeam;

    @Column(name = "redTeamScore")
    private Integer redTeamScore;

    @Column(name = "blueTeamScore")
    private Integer blueTeamScore;

    public Match(Team redTeam, Team blueTeam, Integer redTeamScore, Integer blueTeamScore){
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
        this.redTeamScore = redTeamScore;
        this.blueTeamScore = blueTeamScore;
    }

}
