package EloRatingSystem.Achievement.Repositories;

import EloRatingSystem.Achievement.Models.GameType;
import EloRatingSystem.Achievement.Models.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement,Long> {
    Optional<PlayerAchievement> findByPlayerIdAndAchievementId(Long playerId, Long achievementId);
    List<PlayerAchievement> findAllByPlayerId(Long playerId);
    List<PlayerAchievement> findAllByPlayerIdAndDateAndGameType(Long playerId, Date date, GameType gameType);

    void deleteAllByPlayerIdAndGameType(Long playerId, GameType gameType);
}
