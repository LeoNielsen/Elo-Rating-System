package EloRatingSystem.Reporitories.Achievements;

import EloRatingSystem.Models.Achievement.GameType;
import EloRatingSystem.Models.Achievement.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement,Long> {
    Optional<PlayerAchievement> findByPlayerIdAndAchievementId(Long playerId, Long achievementId);
    List<PlayerAchievement> findAllByPlayerId(Long playerId);

    void deleteAllByPlayerIdAndGameType(Long playerId, GameType gameType);
}
