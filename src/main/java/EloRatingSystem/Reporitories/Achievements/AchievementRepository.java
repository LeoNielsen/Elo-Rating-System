package EloRatingSystem.Reporitories.Achievements;

import EloRatingSystem.Models.Achievement.Achievement;
import EloRatingSystem.Models.Achievement.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement,Long> {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findAllByGameType(GameType gameType);
}
