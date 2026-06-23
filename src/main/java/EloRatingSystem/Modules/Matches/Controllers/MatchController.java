package EloRatingSystem.Modules.Matches.Controllers;

import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Modules.Matches.Services.MatchDeleteService;
import EloRatingSystem.Modules.Stats.Dtos.MatchStatisticsDto;
import EloRatingSystem.Modules.Matches.Services.MatchService;
import EloRatingSystem.Modules.Matches.Services.SoloMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("match")
@Slf4j
public class MatchController {

    @Autowired
    MatchService matchService;
    @Autowired
    MatchDeleteService matchDeleteService;
    @Autowired
    SoloMatchService soloMatchService;

    @GetMapping("/{id}")
    public Mono<TeamMatchResponseDto> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    @GetMapping()
    public Mono<List<TeamMatchResponseDto>> getRecentMatches() {
        return matchService.getRecentMatches();
    }

    @GetMapping("/all")
    public Mono<List<TeamMatchResponseDto>> getAllMatches() {
        return matchService.getAllMatches();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<TeamMatchResponseDto> newMatch(@RequestBody TeamMatchRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return matchService.newMatch(requestDto,username);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/delete/{id}")
    public void deleteMatchById(@PathVariable Long id) {
        matchDeleteService.deleteMatchById(id);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isOwner(#id, authentication)")
    @PutMapping("/update/{id}")
    public void updateMatchById(@PathVariable Long id,@RequestBody TeamMatchRequestDto requestDto) {
        matchDeleteService.updateMatchById(id,requestDto);
    }

    @GetMapping("/solo/{id}")
    public Mono<SoloMatchResponseDto> getSoloMatchById(@PathVariable Long id) {
        return soloMatchService.getSoloMatchById(id);
    }

    @GetMapping("/solo")
    public Mono<List<SoloMatchResponseDto>> getRecentSoloMatches() {
        return soloMatchService.getRecentMatches();
    }

    @PostMapping("/solo/new")
    public Mono<SoloMatchResponseDto> newSoloMatch(@RequestBody SoloMatchRequestDto requestDto) {
        return soloMatchService.newSoloMatch(requestDto);
    }

    @GetMapping("/statistics")
    public Mono<MatchStatisticsDto> matchStatistics() {
        return matchService.getStatistics();
    }
    @GetMapping("/solo/statistics")
    public Mono<MatchStatisticsDto> matchSoloStatistics() {
        return soloMatchService.getSoloStatistics();
    }

}
