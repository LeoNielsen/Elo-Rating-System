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
@Table(name = "Match")
public class Match {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "date")
    private Date date;

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Team redTeam;

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Team blueTeam;

    @Column(name = "redTeamScore")
    private Integer redTeamScore;

    @Column(name = "blueTeamScore")
    private Integer blueTeamScore;

    public Match(Date date, Team redTeam, Team blueTeam, Integer redTeamScore, Integer blueTeamScore){
        this.date = date;
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
        this.redTeamScore = redTeamScore;
        this.blueTeamScore = blueTeamScore;
    }

}
