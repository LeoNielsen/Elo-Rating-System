package EloRatingSystem.Controllers;

import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Services.MatchDeleteService;
import EloRatingSystem.Modules.Matches.Services.MatchService;
import EloRatingSystem.Modules.Matches.Services.SoloMatchService;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.Monthly.Services.MonthlyService;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import EloRatingSystem.Services.*;
import EloRatingSystem.Modules.player.Services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/admin")
@Slf4j
@PreAuthorize("hasRole('admin')")
public class AdminController {
    @Autowired
    MatchService matchService;
    @Autowired
    MatchDeleteService matchDeleteService;
    @Autowired
    PlayerService playerService;
    @Autowired
    MonthlyService monthlyService;
    @Autowired
    SoloMatchService soloMatchService;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    RegenerateService regenerateService;

    @DeleteMapping("/match/latest")
    public void deleteLatestMatch() {
        matchDeleteService.deleteLatestMatch();
    }
    @DeleteMapping("/match/delete/{id}")
    public void deleteMatchById(@PathVariable Long id) {
        matchDeleteService.deleteMatchById(id);
    }

    @DeleteMapping("/match/solo/latest")
    public void deleteLatestSoloMatch() {
        matchDeleteService.deleteLatestSoloMatch();
    }

    @GetMapping("/test")
    public String secured() {
        return "Hello! From Admin";
    }

    @PutMapping("/player/activation/{nameTag}")
    public void deactivatePlayerById(@PathVariable String nameTag) {
        playerService.deactivatePlayerByNameTag(nameTag);
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

    @PostMapping("/player/statgen")
    public void playerStatGen() {
        regenerateService.playerStatisticsGenAll();
        regenerateService.monthlyStatisticsGenAll();
    }

    @PostMapping("/solo/player/statgen")
    public void soloPlayerStatGen() {
        regenerateService.regenerateSoloPlayerStatisticsAll();
    }

    @PostMapping("/monthly/player/statgen")
    public void monthlyStatGen() {
        regenerateService.monthlyStatisticsGenAll();
    }

    @GetMapping("/match/gen")
    public void matchGen() {
        List<Player> players = playerRepository.findAll();
        Random rand = new Random();

        int x = 1000;
        for (int i = 0; i < x; i++) {
            Collections.shuffle(players);
            matchService.newMatch(new TeamMatchRequestDto(players.get(1).getId(), players.get(2).getId(), players.get(3).getId(), players.get(4).getId(), 10, rand.nextInt(0, 10)),"admin");
        }
    }

    @GetMapping("/solo/match/gen")
    public void matchSoloGen() {
        List<Player> players = playerRepository.findAll();
        Random rand = new Random();

        int x = 10;
        for (int i = 0; i < x; i++) {
            Collections.shuffle(players);
            soloMatchService.newSoloMatch(new SoloMatchRequestDto(players.get(0).getId(), players.get(1).getId(), 10, rand.nextInt(0, 10)),"admin");
        }
    }

    @GetMapping("/winner")
    public void getMonthlyWinner() {
        monthlyService.setMonthlyWinner();
    }
}
