package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.MatchDtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchDtos.SoloMatchRequestDto;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Services.*;
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
    MonthlyService monthlyService;
    @Autowired
    SoloMatchService soloMatchService;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    RegenerateService regenerateService;
    @Autowired
    TeamService teamService;

    @DeleteMapping("/match/latest")
    public void deleteLatestMatch() {
        matchService.deleteLatestMatch();
    }

    @DeleteMapping("/match/solo/latest")
    public void deleteLatestSoloMatch() {
        soloMatchService.deleteLatestSoloMatch();
    }

    @GetMapping("/test")
    public String secured() {
        return "Hello! From Admin";
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
            matchService.newMatch(new MatchRequestDto(players.get(1).getId(), players.get(2).getId(), players.get(3).getId(), players.get(4).getId(), 10, rand.nextInt(0, 10)));
        }
    }

    @GetMapping("/solo/match/gen")
    public void matchSoloGen() {
        List<Player> players = playerRepository.findAll();
        Random rand = new Random();

        int x = 10;
        for (int i = 0; i < x; i++) {
            Collections.shuffle(players);
            soloMatchService.newSoloMatch(new SoloMatchRequestDto(players.get(0).getId(), players.get(1).getId(), 10, rand.nextInt(0, 10)));
        }
    }

    @GetMapping("/winner")
    public void getMonthlyWinner() {
        monthlyService.setMonthlyWinner();
    }
}
