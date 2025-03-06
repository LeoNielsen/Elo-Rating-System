package EloRatingSystem.Reporitories;

import EloRatingSystem.Models.SoloPlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoloRatingRepository extends JpaRepository<SoloPlayerRating,Long> {

    List<SoloPlayerRating> findAllBySoloMatchId(Long id);
}
