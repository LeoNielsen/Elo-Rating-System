package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.SoloMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoloMatchRepository extends JpaRepository<SoloMatch,Long> {
    Optional<SoloMatch> findTop1ByOrderByIdDesc();
    List<SoloMatch> findAllByDateAndRedPlayerIdOrDateAndBluePlayerId(LocalDate date, Long redId, LocalDate date2, Long blueId);

    List<SoloMatch> findAllByRedPlayerIdOrBluePlayerId(Long redId,Long blueId);
    List<SoloMatch> findTop100ByOrderByIdDesc();
}
