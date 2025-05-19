package EloRatingSystem.Controllers;

import EloRatingSystem.Models.Achievement.Achievement;
import EloRatingSystem.Models.Achievement.PlayerAchievement;
import EloRatingSystem.Reporitories.Achievements.AchievementRepository;
import EloRatingSystem.Reporitories.Achievements.PlayerAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    AchievementRepository achievementRepository;
    @Autowired
    PlayerAchievementRepository playerAchievementRepository;

    @GetMapping("/all")
    public Mono<List<Achievement>> getAllAchievements(){
        return Mono.just(achievementRepository.findAll());
    }

    @GetMapping("/{playerId}")
    public Mono<List<PlayerAchievement>> getPlayerAchievements(@PathVariable Long playerId){
        return Mono.just(playerAchievementRepository.findAllByPlayerId(playerId));
    }


}
