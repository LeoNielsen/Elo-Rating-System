package EloRatingSystem.Modules.Team.Repositories;

import EloRatingSystem.Modules.Team.Models.TeamPair;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface TeamPairRepository extends JpaRepository<TeamPair, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "playerA",
            "playerB",
            "teams"
    })
    List<TeamPair> findAll();

    Optional<TeamPair> findByPlayerAIdAndPlayerBId(long playerA, long playerBId);

}
