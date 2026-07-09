package EloRatingSystem.Modules.Achievement.Repositories;

import EloRatingSystem.Modules.Achievement.Models.GameType;
import EloRatingSystem.Modules.Achievement.Models.PlayerAchievement;
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

    void deleteAllByMatchId(Long matchId);

    void deleteAllBySoloMatchId(Long soloMatchId);

    void deleteAllByPlayerIdAndGameType(Long playerId, GameType gameType);
}
