package EloRatingSystem.Modules.Matches.Models;

import EloRatingSystem.Modules.Team.Models.Team;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "Match")
public class TeamMatch extends BaseMatch {

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Team redTeam;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Team blueTeam;


    public TeamMatch(Date date, Team redTeam, Team blueTeam, Integer redTeamScore, Integer blueTeamScore) {
        super(date, redTeamScore, blueTeamScore);
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
    }

}
