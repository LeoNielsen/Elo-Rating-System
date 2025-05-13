package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.SoloPlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoloPlayerStatsRepository extends JpaRepository<SoloPlayerStats, Long> {
    Optional<SoloPlayerStats> findByPlayerId(Long PlayerId);
}
