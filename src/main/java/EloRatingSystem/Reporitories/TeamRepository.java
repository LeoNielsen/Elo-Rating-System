package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "attacker","defender",
    })
    List<Team> findAll();

    Optional<Team> findByAttackerIdAndDefenderId(Long attacker, Long defender);
    List<Team> findAllByAttackerIdOrDefenderId(Long attacker, Long defender);
}
