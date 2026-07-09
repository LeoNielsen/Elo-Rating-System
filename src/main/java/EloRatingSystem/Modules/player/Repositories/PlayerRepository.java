package EloRatingSystem.Modules.player.Repositories;

import EloRatingSystem.Modules.player.Models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByNameTagIgnoreCase(String name);
}
