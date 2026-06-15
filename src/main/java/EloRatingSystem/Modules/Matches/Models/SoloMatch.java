package EloRatingSystem.Modules.Matches.Models;

import EloRatingSystem.Modules.player.Models.Player;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "SoloMatch")
public class SoloMatch extends BaseMatch {

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Player redPlayer;

    @ManyToOne (cascade = CascadeType.PERSIST)
    private Player bluePlayer;

    public SoloMatch(Date date, Player redPlayer, Player bluePlayer, Integer redScore, Integer blueScore) {
        super(date,redScore, blueScore);
        this.redPlayer = redPlayer;
        this.bluePlayer = bluePlayer;
    }
}
