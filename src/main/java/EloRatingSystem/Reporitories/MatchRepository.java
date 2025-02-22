package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match,Long> {
    List<Match> findAllByRedTeamIdOrBlueTeamId(Long red, Long blue);

    Optional<Match> findTop1ByOrderByIdDesc();
}
