package EloRatingSystem.Modules.Achievement.Repositories;

import EloRatingSystem.Modules.Achievement.Dtos.PlayerAchievementDto;
import EloRatingSystem.Modules.Achievement.Models.GameType;
import EloRatingSystem.Modules.Achievement.Models.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement,Long> {
    Optional<PlayerAchievement> findByPlayerIdAndAchievementId(Long playerId, Long achievementId);
    @Query("""
    select new EloRatingSystem.Modules.Achievement.Dtos.PlayerAchievementDto(
        pa.id,
        pa.player.id,
        pa.achievement,
        pa.unlocked,
        pa.gameType,
        pa.date,
        pa.soloMatch.id,
        pa.match.id
    )
    from PlayerAchievement pa
    where pa.player.id = :playerId
""")
    List<PlayerAchievementDto> findAllByPlayerId(Long playerId);
    List<PlayerAchievement> findAllByPlayerIdAndDateAndGameType(Long playerId, Date date, GameType gameType);

    void deleteAllByMatchId(Long matchId);

    void deleteAllBySoloMatchId(Long soloMatchId);

    void deleteAllByPlayerIdAndGameType(Long playerId, GameType gameType);
}
