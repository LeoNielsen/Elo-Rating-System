package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match,Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    List<Match> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    Optional<Match> findById(@NonNull Long id);
    List<Match> findAllByRedTeamIdOrBlueTeamId(Long red, Long blue);
    List<Match> findAllByDate(Date date);

    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    Optional<Match> findTop1ByOrderByIdDesc();
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    List<Match> findTop100ByOrderByIdDesc();
}
