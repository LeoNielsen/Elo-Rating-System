package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.SoloMatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoloMatchRepository extends JpaRepository<SoloMatch,Long> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "redPlayer","bluePlayer",
    })
    List<SoloMatch> findAll();
    Optional<SoloMatch> findTop1ByOrderByIdDesc();
    List<SoloMatch> findAllByDateAndRedPlayerIdOrDateAndBluePlayerId(LocalDate date, Long redId, LocalDate date2, Long blueId);

    List<SoloMatch> findAllByRedPlayerIdOrBluePlayerId(Long redId,Long blueId);
    List<SoloMatch> findTop100ByOrderByIdDesc();
}
