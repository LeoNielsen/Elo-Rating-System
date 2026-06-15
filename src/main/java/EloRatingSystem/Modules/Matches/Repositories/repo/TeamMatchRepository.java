package EloRatingSystem.Modules.Matches.Repositories.repo;

import EloRatingSystem.Modules.Matches.Models.TeamMatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMatchRepository extends JpaRepository<TeamMatch,Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    List<TeamMatch> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    Optional<TeamMatch> findById(@NonNull Long id);
    List<TeamMatch> findAllByRedTeamIdOrBlueTeamId(Long red, Long blue);
    List<TeamMatch> findAllByDate(Date date);

    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    Optional<TeamMatch> findTop1ByOrderByIdDesc();
    @EntityGraph(attributePaths = {
            "redTeam","blueTeam",
            "redTeam.defender","redTeam.attacker",
            "blueTeam.defender","blueTeam.attacker"
    })
    List<TeamMatch> findTop100ByOrderByIdDesc();

    List<TeamMatch> findAllByDateBetween(Date start, Date end);
}
