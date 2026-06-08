package EloRatingSystem.Modules.Achievement.Repositories;

import EloRatingSystem.Modules.Achievement.Models.Achievement;
import EloRatingSystem.Modules.Achievement.Models.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement,Long> {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findAllByGameType(GameType gameType);
}
