package EloRatingSystem.Controllers;

import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Services.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    @Autowired
    MatchService matchService;
    @Autowired
    PlayerRepository playerRepository;

    @DeleteMapping("/match/latest")
    public void deleteLatestMatch() {
        matchService.deleteLatestMatch();
    }

    @DeleteMapping("/match/solo/latest")
    public void deleteLatestSoloMatch() {
        matchService.deleteLatestSoloMatch();
    }

    @GetMapping("/test")
    public String secured(){
        return "Hello ! this is a private page !";
    }

    // Only works on players that haven't played any games
    @DeleteMapping("/player/{id}")
    public void deletePlayerById(@PathVariable Long id) {
        playerRepository.deleteById(id);
    }

    @DeleteMapping("/player/all")
    public void deletePlayers() {
        playerRepository.deleteAll();
    }

}
